package sttp.openai

import sttp.capabilities.Streams
import sttp.client4._
import sttp.model.Uri
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.json.SttpUpickleApiExtension._
import sttp.openai.requests.claude.ClaudeRequestBody.ClaudeMessageRequest
import sttp.openai.requests.claude.ClaudeResponseData.ClaudeMessageResponse

class Claude(apiKey: String, baseUri: Uri = ClaudeUris.ClaudeBaseUri) {

  private val claudeUris = new ClaudeUris(baseUri)

  /** Creates a message with Claude API.
    *
    * [[https://docs.anthropic.com/en/api/messages]]
    *
    * @param messageRequest
    *   The message request containing model, messages, and other parameters
    */
  def createMessage(messageRequest: ClaudeMessageRequest): Request[Either[OpenAIException, ClaudeMessageResponse]] =
    claudeAuthRequest
      .post(claudeUris.Messages)
      .body(asJson(messageRequest))
      .response(asJson_parseErrors[ClaudeMessageResponse])

  /** Creates a streaming message with Claude API.
    *
    * [[https://docs.anthropic.com/en/api/messages-streaming]]
    *
    * @param s
    *   Stream capabilities
    * @param messageRequest
    *   The message request with streaming enabled
    */
  def createMessageStream[S](s: Streams[S], messageRequest: ClaudeMessageRequest): StreamRequest[Either[OpenAIException, s.BinaryStream], S] =
    claudeAuthRequest
      .post(claudeUris.Messages)
      .body(asJson(messageRequest.copy(stream = Some(true))))
      .response(asStreamUnsafe_parseErrors(s))

  /** Creates a streaming message with Claude API as InputStream.
    *
    * [[https://docs.anthropic.com/en/api/messages-streaming]]
    *
    * @param messageRequest
    *   The message request with streaming enabled
    */
  def createMessageAsInputStream(messageRequest: ClaudeMessageRequest): Request[Either[OpenAIException, java.io.InputStream]] =
    claudeAuthRequest
      .post(claudeUris.Messages)
      .body(asJson(messageRequest.copy(stream = Some(true))))
      .response(asInputStreamUnsafe_parseErrors)

  protected def claudeAuthRequest: PartialRequest[Either[String, String]] = basicRequest
    .header("x-api-key", apiKey)
    .header("anthropic-version", "2023-06-01")
    .header("content-type", "application/json")
}

class ClaudeUris(baseUri: Uri) {
  val Messages: Uri = baseUri.addPath("messages")
}

object ClaudeUris {
  val ClaudeBaseUri: Uri = uri"https://api.anthropic.com/v1"
}