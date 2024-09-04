package sttp.openai.json

import sttp.client4.json._
import sttp.client4.upicklejson.SttpUpickleApi
import sttp.client4._
import sttp.model.StatusCode._
import sttp.model.ResponseMetadata
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.OpenAIExceptions.OpenAIException._
import sttp.capabilities.Streams

import java.io.InputStream

/** An sttp upickle api extension that deserializes JSON with snake_case keys into case classes with fields corresponding to keys in
  * camelCase and maps errors to OpenAIException subclasses.
  */
object SttpUpickleApiExtension extends SttpUpickleApi {
  override val upickleApi: SnakePickle.type = SnakePickle

  def asStreamUnsafe_parseErrors[S](s: Streams[S]): StreamResponseAs[Either[OpenAIException, s.BinaryStream], S] =
    asStreamUnsafe(s).mapWithMetadata { (body, meta) =>
      body.left.map(errorBody => httpToOpenAIError(HttpError(errorBody, meta.code)))
    }

  def asInputStreamUnsafe_parseErrors: ResponseAs[Either[OpenAIException, InputStream]] =
    asInputStreamUnsafe.mapWithMetadata { (body, meta) =>
      body.left.map(errorBody => httpToOpenAIError(HttpError(errorBody, meta.code)))
    }

  def asJson_parseErrors[B: upickleApi.Reader: IsOption]: ResponseAs[Either[OpenAIException, B]] =
    asString.mapWithMetadata(deserializeRightWithMappedExceptions(deserializeJsonSnake)).showAsJson

  private def deserializeRightWithMappedExceptions[T](
      doDeserialize: String => Either[DeserializationOpenAIException, T]
  ): (Either[String, String], ResponseMetadata) => Either[OpenAIException, T] = {
    case (Left(body), meta) =>
      Left(httpToOpenAIError(HttpError(body, meta.code)))
    case (Right(body), _) => doDeserialize.apply(body)
  }

  def deserializeJsonSnake[B: upickleApi.Reader: IsOption]: String => Either[DeserializationOpenAIException, B] = { (s: String) =>
    try
      Right(upickleApi.read[B](JsonInput.sanitize[B].apply(s)))
    catch {
      case e: Exception => Left(DeserializationOpenAIException(e))
      case t: Throwable =>
        // in ScalaJS, ArrayIndexOutOfBoundsException exceptions are wrapped in org.scalajs.linker.runtime.UndefinedBehaviorError
        t.getCause match {
          case e: ArrayIndexOutOfBoundsException => Left(DeserializationOpenAIException(e))
          case _                                 => throw t
        }
    }
  }

  def asStringEither: ResponseAs[Either[OpenAIException, String]] =
    asStringAlways
      .mapWithMetadata { (string, metadata) =>
        if (metadata.isSuccess) Right(string) else Left(httpToOpenAIError(HttpError(string, metadata.code)))
      }
      .showAs("either(as error, as string)")

  private def httpToOpenAIError(he: HttpError[String]): OpenAIException = {
    val errorMessageBody = upickleApi.read[ujson.Value](he.body).apply("error")
    val error = upickleApi.read[Error](errorMessageBody)
    import error._

    he.statusCode match {
      case TooManyRequests                              => new RateLimitException(message, `type`, param, code, he)
      case BadRequest | NotFound | UnsupportedMediaType => new InvalidRequestException(message, `type`, param, code, he)
      case Unauthorized                                 => new AuthenticationException(message, `type`, param, code, he)
      case Forbidden                                    => new PermissionException(message, `type`, param, code, he)
      case Conflict                                     => new TryAgain(message, `type`, param, code, he)
      case ServiceUnavailable                           => new ServiceUnavailableException(message, `type`, param, code, he)
      case _                                            => new APIException(message, `type`, param, code, he)
    }
  }

  private case class Error(
      message: Option[String] = None,
      `type`: Option[String] = None,
      param: Option[String] = None,
      code: Option[String] = None
  )
  private object Error {
    implicit val errorR: upickleApi.Reader[Error] = upickleApi.macroR
  }
}
