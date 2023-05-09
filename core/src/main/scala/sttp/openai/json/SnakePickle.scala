package sttp.openai.json

import sttp.client4.json._
import sttp.client4.upicklejson.SttpUpickleApi
import sttp.client4._
import sttp.model.{MediaType, ResponseMetadata}
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

/** This is required in order to deserialize JSON with snake_case keys into case classes with fields corresponding to keys in camelCase */
object SttpUpickleApiExtension extends SttpUpickleApi {
  implicit def upickleBodySerializerSnake[B](implicit encoder: SnakePickle.Writer[B]): BodySerializer[B] =
    b => StringBody(SnakePickle.write(b), "utf-8", MediaType.ApplicationJson)

  def asJsonSnake[B: SnakePickle.Reader: IsOption]: ResponseAs[Either[OpenAIException, B]] =
    asString.mapWithMetadata(deserializeRightWithMappedExceptions(deserializeJsonSnake)).showAsJson

  private def deserializeRightWithMappedExceptions[T](
      doDeserialize: String => Either[DeserializationOpenAIException, T]
  ): (Either[String, String], ResponseMetadata) => Either[OpenAIException, T] = {
    case (Left(body), meta) =>
      Left(httpToOpenAIError(HttpError(body, meta.code)))
    case (Right(body), _) => doDeserialize.apply(body)
  }

  def deserializeJsonSnake[B: SnakePickle.Reader: IsOption]: String => Either[DeserializationOpenAIException, B] = { (s: String) =>
    try
      Right(SnakePickle.read[B](JsonInput.sanitize[B].apply(s)))
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
    val errorMessageBody = SnakePickle.read[ujson.Value](he.body).apply("error").obj.values
    val (message, typ, param, code) =
      SnakePickle.read[(Option[String], Option[String], Option[String], Option[String])](errorMessageBody)
    he.statusCode match {
      case TooManyRequests                              => RateLimitException(message, typ, param, code, he)
      case BadRequest | NotFound | UnsupportedMediaType => InvalidRequestException(message, typ, param, code, he)
      case Unauthorized                                 => AuthenticationException(message, typ, param, code, he)
      case Forbidden                                    => PermissionException(message, typ, param, code, he)
      case Conflict                                     => TryAgain(message, typ, param, code, he)
      case ServiceUnavailable                           => ServiceUnavailableException(message, typ, param, code, he)
      case _                                            => APIException(message, typ, param, code, he)
    }
  }

}
