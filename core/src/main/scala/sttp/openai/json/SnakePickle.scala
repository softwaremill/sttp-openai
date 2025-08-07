package sttp.openai.json

import ujson._

/** An object that transforms all snake_case keys into camelCase [[https://com-lihaoyi.github.io/upickle/#CustomConfiguration]] */
object SnakePickle extends upickle.AttributeTagged {

  override def tagName: String = "type"

  private def camelToSnake(s: String): String =
    s.replaceAll("([A-Z])", "#$1").split('#').map(_.toLowerCase).mkString("_")

  override def objectAttributeKeyReadMap(s: CharSequence): String =
    SerializationHelpers.snakeToCamel(s.toString)

  override def objectAttributeKeyWriteMap(s: CharSequence): String =
    camelToSnake(s.toString)
//
//  override def objectTypeKeyReadMap(s: CharSequence): String =
//    SerializationHelpers.snakeToCamel(s.toString)

//  override def objectTypeKeyWriteMap(s: CharSequence): String =
//    camelToSnake(s.toString)

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
  def snakeToCamel(s: String): String =
    if (s.isEmpty) s
    else {
      val parts = s.split('_').filter(_.nonEmpty)
      val startsWithUnderscore = s.startsWith("_")

      parts.zipWithIndex.map { case (part, index) =>
        if (index == 0 && !startsWithUnderscore) {
          s"${part.head.toLower}${part.tail}"
        } else {
          s"${part.head.toUpper}${part.tail}"
        }
      }.mkString
    }

  case class DiscriminatorField(value: String)

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
  def withNestedDiscriminator[T](discriminatorField: DiscriminatorField, discriminatorValue: String, nestedField: String)(implicit
      baseRW: SnakePickle.Writer[T]
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
            Obj.from(filtered)
          case other => other
        }
        Obj(
          discriminatorField.value -> discriminatorValue,
          nestedField -> cleanedJson
        )
      }

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
//  def withFlattenedDiscriminator[T](discriminatorField: DiscriminatorField, discriminatorValue: String)(implicit
//      baseW: SnakePickle.Writer[T]
//  ): SnakePickle.Writer[T] =
//    SnakePickle
//      .writer[Value]
//      .comap { t =>
//        val baseJson = SnakePickle.writeJs(t)
//        // Filter out any $type fields and null values (from None options)
//        val cleanedJson = baseJson match {
//          case obj: Obj =>
//            val filtered = obj.obj.filterNot { case (key, value) =>
//              key == Str(SnakePickle.tagName) || value == ujson.Null
//            }
//            // Add the discriminator field to the filtered object
//            Obj.from(filtered ++ Map(discriminatorField.value -> Str(discriminatorValue)))
//          case other =>
//            // If it's not an object, create a new object with the discriminator and the value
//            Obj(discriminatorField.value -> Str(discriminatorValue), "value" -> other)
//        }
//        cleanedJson
//      }

  def withFlattenedDiscriminatorReader[T](discriminatorField: DiscriminatorField, baseR: SnakePickle.Reader[T])(implicit
      classTag: scala.reflect.ClassTag[T]
  ): SnakePickle.Reader[T] =
    SnakePickle
      .reader[Value]
      .map { json =>
//        val className = classTag.runtimeClass.getSimpleName
//        // This is a workaround for AttributeTagged Reader limitations.
//        // While upickle supports different discriminator fields for different types,
//        // it defaults to using the class name as the discriminator value with no built-in way to customize it.
//        // Our approach uses a custom discriminator field alongside upickle's internal one.
//        // The read discriminator value can be ignored as validation happens at the sealed trait Reader level.
//        val jsonWithUpickleType = json.obj.addOne(SnakePickle.tagName -> Str(className))
//        SnakePickle.read[T](Obj(jsonWithUpickleType))(baseR)
        SnakePickle.read[T](json)(baseR)
      }

  def caseObjectWithDiscriminatorWriter[T](discriminatorField: DiscriminatorField, discriminatorValue: String): SnakePickle.Writer[T] =
    SnakePickle.writer[Value].comap(_ => Obj(discriminatorField.value -> Str(discriminatorValue)))

}
