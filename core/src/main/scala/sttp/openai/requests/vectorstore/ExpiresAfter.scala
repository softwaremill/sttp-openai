package sttp.openai.requests.vectorstore

import sttp.openai.json.SnakePickle
import ujson.{Obj, Value}

case class ExpiresAfter(anchor: String, days: Int)
object ExpiresAfter {

  implicit val expiresAfterRW: SnakePickle.ReadWriter[ExpiresAfter] = SnakePickle
    .readwriter[Value]
    .bimap[ExpiresAfter](
      ea => Obj("anchor" -> ea.anchor, "days" -> ea.days),
      json => ExpiresAfter(json("anchor").str, json("days").num.toInt)
    )
}