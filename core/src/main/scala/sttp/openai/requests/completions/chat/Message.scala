package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle

/** @param role
  *   The role of the author of this message. One of `system`, `user`, or `assistant`.
  * @param content
  *   The contents of the message.
  * @param name
  *   The name of the author of this message. May contain a-z, A-Z, 0-9, and underscores, with a maximum length of 64 characters.
  */
case class Message(role: String, content: String, name: Option[String] = None)

object Message {
  implicit val messageRW: SnakePickle.ReadWriter[Message] = SnakePickle.macroRW[Message]
}
