package sttp.openai.json

import ujson._

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

/** Helper utilities for automatic serialization with discriminator fields */
object SerializationHelpers {

  /** Creates a ReadWriter for nested discriminator patterns where the object is wrapped in another object with a discriminator field
    * pointing to the nested content.
    *
    * For example: {"type": "json_schema", "json_schema": {...actual object...}}
    *
    * @param discriminatorField
    *   The name of the field to add (e.g., "type")
    * @param discriminatorValue
    *   The value for the discriminator field (e.g., "json_schema")
    * @param nestedField
    *   The name of the field containing the nested object (e.g., "json_schema")
    * @param baseRW
    *   The base ReadWriter for the type T (typically SnakePickle.macroRW)
    * @return
    *   A ReadWriter that wraps the object in the nested discriminator structure
    */
  def withNestedDiscriminator[T](discriminatorField: String, discriminatorValue: String, nestedField: String)(implicit
      baseRW: SnakePickle.ReadWriter[T]
  ): SnakePickle.ReadWriter[T] =
    SnakePickle
      .readwriter[Value]
      .bimap[T](
        t => {
          val baseJson = SnakePickle.writeJs(t)
          // Filter out any $type fields and null values (from None options)
          val cleanedJson = baseJson match {
            case obj: Obj =>
              val filtered = obj.obj.filterNot { case (key, value) =>
                key.startsWith("$") || value == ujson.Null
              }
              Obj.from(filtered)
            case other => other
          }
          Obj(
            discriminatorField -> discriminatorValue,
            nestedField -> cleanedJson
          )
        },
        json => SnakePickle.read[T](json(nestedField))
      )

  /** Creates a ReadWriter for flattened discriminator patterns where the object fields are at the same level as the discriminator field.
    *
    * For example: {"type": "json_schema", ...actual object fields...}
    *
    * @param discriminatorField
    *   The name of the field to add (e.g., "type")
    * @param discriminatorValue
    *   The value for the discriminator field (e.g., "json_schema")
    * @param baseW
    *   The base Writer for the type T (typically SnakePickle.macroW)
    * @return
    *   A ReadWriter that flattens the object with the discriminator field
    */
  def withFlattenedDiscriminator[T](discriminatorField: String, discriminatorValue: String)(implicit
      baseW: SnakePickle.Writer[T]
  ): SnakePickle.Writer[T] =
    SnakePickle
      .writer[Value]
      .comap { t =>
        val baseJson = SnakePickle.writeJs(t)
        // Filter out any $type fields and null values (from None options)
        val cleanedJson = baseJson match {
          case obj: Obj =>
            val filtered = obj.obj.filterNot { case (key, value) =>
              key.startsWith("$") || value == ujson.Null
            }
            // Add the discriminator field to the filtered object
            Obj.from(filtered ++ Map(discriminatorField -> Str(discriminatorValue)))
          case other =>
            // If it's not an object, create a new object with the discriminator and the value
            Obj(discriminatorField -> Str(discriminatorValue), "value" -> other)
        }
        cleanedJson
      }

  def caseObjectWithDiscriminatorWriter[T](discriminatorField: String, discriminatorValue: String): SnakePickle.Writer[T] =
    SnakePickle.writer[Value].comap(_ => Obj(discriminatorField -> Str(discriminatorValue)))

}
