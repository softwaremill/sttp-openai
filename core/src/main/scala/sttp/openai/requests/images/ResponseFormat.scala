package sttp.openai.requests.images

import sttp.openai.json.SnakePickle
import sttp.client4.DeserializationException
import ujson.Str

sealed abstract class ResponseFormat(val value: String)

object ResponseFormat {
  case object URL extends ResponseFormat("url")

  case object B64Json extends ResponseFormat("b64_json")

  /** Use only as a workaround if API supports a format that's not yet predefined as a case object of Response Format. Otherwise, a custom
    * format would be rejected. See [[https://platform.openai.com/docs/api-reference/images/create-edit]] for current list of supported
    * formats
    */
  case class Custom(customResponseFormat: String) extends ResponseFormat(customResponseFormat)

  val values: Set[ResponseFormat] = Set(URL, B64Json)

  private val byValue =
    values.map(s => (s.value, s)).toMap

  private def withValue(responseFormat: String): Option[ResponseFormat] = byValue.get(responseFormat)

  implicit val responseFormatRW: SnakePickle.ReadWriter[ResponseFormat] = SnakePickle
    .readwriter[ujson.Value]
    .bimap[ResponseFormat](
      _.value,
      jsonValue =>
        SnakePickle.read[ujson.Value](jsonValue) match {
          case Str(value) =>
            withValue(value) match {
              case Some(responseFormat) => responseFormat
              case None                 => Custom(value)
            }
          case e => throw DeserializationException(e.str, new Exception(s"Could not deserialize: $e"))
        }
    )
}
