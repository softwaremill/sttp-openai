package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.Usage

object ChatRequestResponseData {
  case class Choices(
      message: Message,
      finishReason: String,
      index: Int
  )

  object Choices {
    implicit val choicesRW: SnakePickle.ReadWriter[Choices] = SnakePickle.macroRW[Choices]
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
    implicit val chatResponseRW: SnakePickle.ReadWriter[ChatResponse] = SnakePickle.macroRW[ChatResponse]
  }

}
