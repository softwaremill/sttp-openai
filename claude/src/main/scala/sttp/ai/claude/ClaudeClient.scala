package sttp.ai.claude

import sttp.ai.claude.ClaudeExceptions.ClaudeException
import sttp.ai.claude.config.ClaudeConfig
import sttp.ai.claude.requests.MessageRequest
import sttp.ai.claude.responses.{MessageResponse, ModelsResponse}
import sttp.client4._
import sttp.model.Uri
import upickle.default._

trait ClaudeClient {
  def createMessage(request: MessageRequest): Request[Either[ClaudeException, MessageResponse]]
  def listModels(): Request[Either[ClaudeException, ModelsResponse]]
}

class ClaudeClientImpl(config: ClaudeConfig) extends ClaudeClient {

  private val claudeUris = new ClaudeUris(config.baseUrl)

  private def claudeAuthRequest =
    basicRequest
      .header("x-api-key", config.apiKey)
      .header("anthropic-version", config.anthropicVersion)
      .header("content-type", "application/json")

  private def asJson_parseErrors[T: Reader]: ResponseAs[Either[ClaudeException, T]] =
    asString.mapWithMetadata { (responseBody, metadata) =>
      responseBody match {
        case Left(error) =>
          Left(
            ClaudeException.DeserializationClaudeException(
              new Exception(error),
              metadata
            )
          )
        case Right(body) =>
          try
            Right(read[T](body))
          catch {
            case e: Exception =>
              try {
                val errorResponse = read[sttp.ai.claude.responses.ErrorResponse](body)
                Left(mapErrorToException(errorResponse, metadata))
              } catch {
                case _: Exception =>
                  Left(ClaudeException.DeserializationClaudeException(e, metadata))
              }
          }
      }
    }

  private def mapErrorToException(
      errorResponse: sttp.ai.claude.responses.ErrorResponse,
      metadata: sttp.model.ResponseMetadata
  ): ClaudeException = {
    val error = errorResponse.error
    val cause = ResponseException.UnexpectedStatusCode(error.message, metadata)

    error.`type` match {
      case "authentication_error" =>
        new ClaudeException.AuthenticationException(
          Some(error.message),
          Some(error.`type`),
          None,
          None,
          cause
        )
      case "permission_error" =>
        new ClaudeException.PermissionException(
          Some(error.message),
          Some(error.`type`),
          None,
          None,
          cause
        )
      case "rate_limit_error" =>
        new ClaudeException.RateLimitException(
          Some(error.message),
          Some(error.`type`),
          None,
          None,
          cause
        )
      case "invalid_request_error" =>
        new ClaudeException.InvalidRequestException(
          Some(error.message),
          Some(error.`type`),
          None,
          None,
          cause
        )
      case _ =>
        new ClaudeException.APIException(
          Some(error.message),
          Some(error.`type`),
          None,
          None,
          cause
        )
    }
  }

  override def createMessage(request: MessageRequest): Request[Either[ClaudeException, MessageResponse]] =
    claudeAuthRequest
      .post(claudeUris.Messages)
      .body(write(request))
      .response(asJson_parseErrors[MessageResponse])

  override def listModels(): Request[Either[ClaudeException, ModelsResponse]] =
    claudeAuthRequest
      .get(claudeUris.Models)
      .response(asJson_parseErrors[ModelsResponse])
}

class ClaudeUris(baseUri: Uri) {
  val Messages: Uri = baseUri.addPath("v1", "messages")
  val Models: Uri = baseUri.addPath("v1", "models")
}

object ClaudeClient {
  def apply(config: ClaudeConfig): ClaudeClientImpl = new ClaudeClientImpl(config)
}
