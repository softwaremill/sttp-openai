package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle

object ChatChunkRequestResponseData {

  /** @param role
    *   The role of the author of this message.
    * @param content
    *   The contents of the message.
    * @param functionCall
    *   The name of the author of this message. May contain a-z, A-Z, 0-9, and underscores, with a maximum length of 64 characters.
    */
  case class Delta(role: Option[Role] = None, content: Option[String] = None, functionCall: Option[FunctionCall] = None)

  object Delta {
    implicit val deltaR: SnakePickle.Reader[Delta] = SnakePickle.macroR[Delta]
  }

  case class Choices(
      delta: Delta,
      finishReason: String,
      index: Int
  )

  object Choices {
    implicit val choicesR: SnakePickle.Reader[Choices] = SnakePickle.macroR[Choices]
  }

  case class ChatChunkResponse(
      id: String,
      `object`: String,
      created: Int,
      model: String,
      choices: Seq[Choices]
  )

  object ChatChunkResponse {
    val DoneEventMessage = "[DONE]"

    implicit val chunkChatR: SnakePickle.Reader[ChatChunkResponse] = SnakePickle.macroR[ChatChunkResponse]
  }

}
