package sttp.openai.requests.completions

import sttp.client4.DeserializationException
import sttp.openai.json.SnakePickle
import ujson.{Arr, Str}

sealed trait Stop
object Stop {
  implicit val stopRW: SnakePickle.ReadWriter[Stop] = SnakePickle
    .readwriter[ujson.Value]
    .bimap[Stop](
      {
        case SingleStop(value)    => SnakePickle.writeJs(value)
        case MultipleStop(values) => SnakePickle.writeJs(values)
      },
      json =>
        SnakePickle.read[ujson.Value](json) match {
          case Str(value)  => SingleStop(value)
          case Arr(values) => MultipleStop(values.map(_.str).toSeq)
          case e           => throw DeserializationException(e.str, new Exception(s"Could not deserialize: $e"))
        }
    )

  case class SingleStop(value: String) extends Stop

  case class MultipleStop(values: Seq[String]) extends Stop
}
