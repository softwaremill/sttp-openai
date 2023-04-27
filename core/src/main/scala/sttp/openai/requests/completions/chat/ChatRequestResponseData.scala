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
    implicit val choicesR: SnakePickle.Reader[Choices] = SnakePickle.macroR
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
    implicit val chatResponseR: SnakePickle.Reader[ChatResponse] = SnakePickle.macroR
  }
}
