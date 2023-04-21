package sttp.openai.requests.images

import sttp.openai.json.SnakePickle
import sttp.client4.DeserializationException
import ujson.Str

sealed abstract class Size(val value: String)

object Size {
  case object Small extends Size("256x256")

  case object Medium extends Size("512x512")

  case object Large extends Size("1024x1024")

  /** Use only as a workaround if API supports a format that's not yet predefined as a case object of Size. Otherwise, a custom format would
    * be rejected. See [[https://platform.openai.com/docs/api-reference/images/create-edit]] for current list of supported formats
    */
  case class Custom(customSize: String) extends Size(customSize)

  val values: Set[Size] = Set(Small, Medium, Large)

  private val byValue =
    values.map(s => (s.value, s)).toMap

  private def withValue(size: String): Option[Size] = byValue.get(size)

  implicit val sizeRW: SnakePickle.ReadWriter[Size] = SnakePickle
    .readwriter[ujson.Value]
    .bimap[Size](
      _.value,
      jsonValue =>
        SnakePickle.read[ujson.Value](jsonValue) match {
          case Str(value) =>
            withValue(value) match {
              case Some(size) => size
              case None       => Custom(value)
            }
          case e => throw DeserializationException(e.str, new Exception(s"Could not deserialize: $e"))
        }
    )
}
