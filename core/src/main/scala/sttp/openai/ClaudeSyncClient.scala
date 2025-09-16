package sttp.openai

import sttp.client4.{DefaultSyncBackend, Request, SyncBackend}
import sttp.model.Uri
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.requests.claude.ClaudeRequestBody.ClaudeMessageRequest
import sttp.openai.requests.claude.ClaudeResponseData.ClaudeMessageResponse

class ClaudeSyncClient private (
    apiKey: String,
    backend: SyncBackend,
    closeClient: Boolean,
    baseUri: Uri,
    customizeRequest: CustomizeClaudeRequest
) {

  private val claude = new Claude(apiKey, baseUri)

  /** Creates a message with Claude API.
    *
    * [[https://docs.anthropic.com/en/api/messages]]
    *
    * @param messageRequest
    *   The message request containing model, messages, and other parameters
    * @throws OpenAIException
    *   when a request fails
    */
  def createMessage(messageRequest: ClaudeMessageRequest): ClaudeMessageResponse =
    sendOrThrow(claude.createMessage(messageRequest))

  /** Closes and releases resources of http client if was not provided explicitly, otherwise works no-op. */
  def close(): Unit = if (closeClient) backend.close() else ()

  /** Specifies a function, which will be applied to the generated request before sending it. If a function has been specified before, it
    * will be applied before the given one.
    */
  def customizeRequest(customize: CustomizeClaudeRequest): ClaudeSyncClient =
    new ClaudeSyncClient(apiKey, backend, closeClient, baseUri, customizeRequest.andThen(customize))

  private def sendOrThrow[A](request: Request[Either[OpenAIException, A]]): A =
    customizeRequest.apply(request).send(backend).body match {
      case Right(value)    => value
      case Left(exception) => throw exception
    }
}

object ClaudeSyncClient {
  def apply(apiKey: String) =
    new ClaudeSyncClient(apiKey, DefaultSyncBackend(), true, ClaudeUris.ClaudeBaseUri, CustomizeClaudeRequest.Identity)
  def apply(apiKey: String, backend: SyncBackend) =
    new ClaudeSyncClient(apiKey, backend, false, ClaudeUris.ClaudeBaseUri, CustomizeClaudeRequest.Identity)
  def apply(apiKey: String, backend: SyncBackend, baseUrl: Uri) =
    new ClaudeSyncClient(apiKey, backend, false, baseUrl, CustomizeClaudeRequest.Identity)
  def apply(apiKey: String, baseUrl: Uri) =
    new ClaudeSyncClient(apiKey, DefaultSyncBackend(), true, baseUrl, CustomizeClaudeRequest.Identity)
}

trait CustomizeClaudeRequest {
  def apply[A](request: Request[Either[OpenAIException, A]]): Request[Either[OpenAIException, A]]

  def andThen(customize: CustomizeClaudeRequest): CustomizeClaudeRequest = new CustomizeClaudeRequest {
    override def apply[A](request: Request[Either[OpenAIException, A]]): Request[Either[OpenAIException, A]] =
      customize.apply(CustomizeClaudeRequest.this(request))
  }
}

object CustomizeClaudeRequest {
  val Identity: CustomizeClaudeRequest = new CustomizeClaudeRequest {
    override def apply[A](request: Request[Either[OpenAIException, A]]): Request[Either[OpenAIException, A]] = request
  }
}
