package sttp.ai.claude.json

/** An object that transforms all snake_case keys into camelCase [[https://com-lihaoyi.github.io/upickle/#CustomConfiguration]] */
object SnakePickle extends upickle.AttributeTagged {

  override def tagName: String = "type"

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

  /** This is required in order to parse null values into Scala's Option */
  override implicit def OptionWriter[T: SnakePickle.Writer]: Writer[Option[T]] =
    implicitly[SnakePickle.Writer[T]].comap[Option[T]] {
      case None    => null.asInstanceOf[T]
      case Some(x) => x
    }

  override implicit def OptionReader[T: SnakePickle.Reader]: Reader[Option[T]] =
    new Reader.Delegate[Any, Option[T]](implicitly[SnakePickle.Reader[T]].map(Some(_))) {
      override def visitNull(index: Int): Option[T] = None
    }
}
