package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.Usage

object ChatRequestResponseData {

  /** @param role
    *   The role of the author of this message.
    * @param content
    *   The contents of the message.
    * @param functionCall
    *   The name of the author of this message. May contain a-z, A-Z, 0-9, and underscores, with a maximum length of 64 characters.
    */
  case class Message(role: Role, content: String, functionCall: Option[FunctionCall] = None)

  object Message {
    implicit val messageRW: SnakePickle.Reader[Message] = SnakePickle.macroR[Message]
  }

  case class Choices(
      message: Message,
      finishReason: String,
      index: Int
  )

  object Choices {
    implicit val choicesR: SnakePickle.Reader[Choices] = SnakePickle.macroR[Choices]
  }

  case class ChatResponse(
      id: String,
      `object`: String,
      created: Int,
      model: String,
      usage: Usage,
      choices: Seq[Choices]
  )

  object ChatResponse {
    implicit val chatResponseR: SnakePickle.Reader[ChatResponse] = SnakePickle.macroR[ChatResponse]
  }

}
