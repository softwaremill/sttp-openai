package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle

case class Message(role: String, content: String)

object Message {
  implicit val messageRW: SnakePickle.ReadWriter[Message] = SnakePickle.macroRW[Message]
}
