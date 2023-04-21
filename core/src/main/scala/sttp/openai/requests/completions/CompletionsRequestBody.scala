package sttp.openai.requests.completions

import sttp.openai.json.SnakePickle

object CompletionsRequestBody {

  case class CompletionsBody(
      model: String,
      prompt: Option[Prompt] = None,
      suffix: Option[String] = None,
      maxTokens: Option[Int] = None,
      temperature: Option[Double] = None,
      topP: Option[Double] = None,
      n: Option[Int] = None,
      stream: Option[Boolean] = None,
      logprobs: Option[Int] = None,
      echo: Option[Boolean] = None,
      stop: Option[Stop] = None,
      presencePenalty: Option[Double] = None,
      frequencyPenalty: Option[Double] = None,
      bestOf: Option[Int] = None,
      logitBias: Option[Map[String, Float]] = None,
      user: Option[String] = None
  )

  object CompletionsBody {
    implicit val completionBodyW: SnakePickle.Writer[CompletionsBody] = SnakePickle.macroW[CompletionsBody]
  }

  sealed trait Prompt
  object Prompt {
    implicit val promptRW: SnakePickle.Writer[Prompt] = SnakePickle
      .writer[ujson.Value]
      .comap[Prompt] {
        case SinglePrompt(value)    => SnakePickle.writeJs(value)
        case MultiplePrompt(values) => SnakePickle.writeJs(values)
      }
  }
  case class SinglePrompt(value: String) extends Prompt
  case class MultiplePrompt(values: Seq[String]) extends Prompt
}
