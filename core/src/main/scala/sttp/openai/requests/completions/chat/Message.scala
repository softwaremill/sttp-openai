package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle

/** @param role
  *   The role of the author of this message. One of `system`, `user`, or `assistant`.
  * @param content
  *   The contents of the message.
  */
case class Message(role: String, content: String, name: Option[String] = None)

object Message {
  implicit val messageRW: SnakePickle.ReadWriter[Message] = SnakePickle.macroRW
}
