package sttp.openai.config

import sttp.client4.json.RichResponseAs
import sttp.client4.upicklejson.SttpUpickleApi
import sttp.client4.{IsOption, JsonInput, ResponseAs, ResponseException, asString}

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

  def asJsonSnake[B: SnakePickle.Reader: IsOption]: ResponseAs[Either[ResponseException[String, Exception], B]] =
    asString.mapWithMetadata(ResponseAs.deserializeRightWithError(deserializeJsonSnake)).showAsJson

  def deserializeJsonSnake[B: SnakePickle.Reader: IsOption]: String => Either[Exception, B] = { (s: String) =>
    try
      Right(SnakePickle.read[B](JsonInput.sanitize[B].apply(s)))
    catch {
      case e: Exception => Left(e)
      case t: Throwable =>
        // in ScalaJS, ArrayIndexOutOfBoundsException exceptions are wrapped in org.scalajs.linker.runtime.UndefinedBehaviorError
        t.getCause match {
          case e: ArrayIndexOutOfBoundsException => Left(e)
          case _                                 => throw t
        }
    }
  }
}
