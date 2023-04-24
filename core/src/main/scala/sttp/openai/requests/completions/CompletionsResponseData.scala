package sttp.openai.requests.completions

import sttp.openai.json.SnakePickle

object CompletionsResponseData {
  case class Choices(
      text: String,
      index: Int,
      logprobs: Option[String],
      finishReason: String
  )
  object Choices {
    implicit val choicesR: SnakePickle.Reader[Choices] = SnakePickle.macroR[Choices]
  }

  case class CompletionsResponse(
      id: String,
      `object`: String,
      created: Int,
      model: String,
      choices: Seq[Choices],
      usage: Usage
  )
  object CompletionsResponse {
    implicit val choicesR: SnakePickle.Reader[CompletionsResponse] = SnakePickle.macroR[CompletionsResponse]
  }
}
