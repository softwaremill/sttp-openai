package sttp.openai.json

import sttp.client4.json._
import sttp.client4.upicklejson.SttpUpickleApi
import sttp.client4._
import sttp.model.ResponseMetadata
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.OpenAIExceptions.OpenAIException._

/** An object that transforms all snake_case keys into camelCase [[https://com-lihaoyi.github.io/upickle/#CustomConfiguration]] */
object SnakePickle extends upickle.AttributeTagged {
  private def camelToSnake(s: String): String =
    s.replaceAll("([A-Z])", "#$1").split('#').map(_.toLowerCase).mkString("_")

  private def snakeToCamel(s: String): String = {
    val res = s.split("_", -1).map(x => s"${x(0).toUpper}${x.drop(1)}").mkString
    s"${s(0).toLower}${res.drop(1)}"
  }

  override def objectAttributeKeyReadMap(s: CharSequence): String =
    snakeToCamel(s.toString)

  override def objectAttributeKeyWriteMap(s: CharSequence): String =
    camelToSnake(s.toString)

  override def objectTypeKeyReadMap(s: CharSequence): String =
    snakeToCamel(s.toString)

  override def objectTypeKeyWriteMap(s: CharSequence): String =
    camelToSnake(s.toString)

  /** This is required in order to parse null values into Scala's Option */
  override implicit def OptionWriter[T: SnakePickle.Writer]: Writer[Option[T]] =
    implicitly[SnakePickle.Writer[T]].comap[Option[T]] {
      case None    => null.asInstanceOf[T]
      case Some(x) => x
    }

  override implicit def OptionReader[T: SnakePickle.Reader]: Reader[Option[T]] =
    new Reader.Delegate[Any, Option[T]](implicitly[SnakePickle.Reader[T]].map(Some(_))) {
      override def visitNull(index: Int) = None
    }
}

/** An sttp upickle api extension that deserializes JSON with snake_case keys into case classes with fields corresponding to keys in
  * camelCase and maps errors to OpenAIException subclasses.
  */
object SttpUpickleApiExtension extends SttpUpickleApi {
  override val upickleApi: SnakePickle.type = SnakePickle

  def asJsonSnake[B: upickleApi.Reader: IsOption]: ResponseAs[Either[OpenAIException, B]] =
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
    import sttp.model.StatusCode._
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
  private case class Error(message: Option[String], `type`: Option[String], param: Option[String], code: Option[String])
  private object Error {
    implicit val errorR: upickleApi.Reader[Error] = upickleApi.macroR
  }
}
