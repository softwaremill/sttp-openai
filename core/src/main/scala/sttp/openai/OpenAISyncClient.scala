package sttp.openai

import sttp.client4.{DefaultSyncBackend, DeserializationException, HttpError, Response, ResponseException, SyncBackend}
import sttp.openai.OpenAIErrors.OpenAIError
import sttp.openai.OpenAIErrors.OpenAIError._
import sttp.openai.json.SnakePickle
import sttp.openai.requests.models.ModelsResponseData.ModelsResponse

class OpenAISyncClient private (authToken: String, backend: SyncBackend) {

  private val openAI = new OpenAI(authToken)

  def getModels: Either[OpenAIError, ModelsResponse] =
    handleOutput(
      openAI.getModels
        .send(backend)
    )
  def close(): Unit = backend.close()

  private def handleOutput[A](
      response: Response[Either[ResponseException[String, Exception], A]]
  ): Either[OpenAIError, A] =
    response.body.left.map {
      case e @ HttpError(body, statusCode) =>
        import sttp.model.StatusCode._
        val errorMessageBody = SnakePickle.read[ujson.Value](body).apply("error").obj.values
        val (message, typ, param, code) =
          SnakePickle.read[(Option[String], Option[String], Option[String], Option[String])](errorMessageBody)
        statusCode match {
          case TooManyRequests                              => RateLimitError(message, typ, param, code, e)
          case BadRequest | NotFound | UnsupportedMediaType => InvalidRequestError(message, typ, param, code, e)
          case Unauthorized                                 => AuthenticationError(message, typ, param, code, e)
          case Forbidden                                    => PermissionError(message, typ, param, code, e)
          case Conflict                                     => TryAgain(message, typ, param, code, e)
          case ServiceUnavailable                           => ServiceUnavailableError(message, typ, param, code, e)
          case _                                            => APIError(message, typ, param, code, e)
        }
      case DeserializationException(_, error) => throw new json.DeserializationException(error)
    }
}

object OpenAISyncClient {
  def apply(authToken: String) = new OpenAISyncClient(authToken, DefaultSyncBackend())
  def apply(authToken: String, backend: SyncBackend) = new OpenAISyncClient(authToken, backend)
}
