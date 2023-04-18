package sttp.openai.requests.completions

import sttp.client4.DeserializationException
import sttp.openai.json.SnakePickle
import ujson.{Arr, Str}

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
    implicit val completionBodyRW: SnakePickle.ReadWriter[CompletionsBody] = SnakePickle.macroRW[CompletionsBody]
  }

  sealed trait Prompt
  object Prompt {
    implicit val promptRW: SnakePickle.ReadWriter[Prompt] = SnakePickle
      .readwriter[ujson.Value]
      .bimap[Prompt](
        {
          case SinglePrompt(value)    => SnakePickle.writeJs(value)
          case MultiplePrompt(values) => SnakePickle.writeJs(values)
        },
        json =>
          SnakePickle.read[ujson.Value](json) match {
            case Str(value)  => SinglePrompt(value)
            case Arr(values) => MultiplePrompt(values.map(_.str).toSeq)
            case e           => throw DeserializationException(e.str, new Exception(s"Could not deserialize: $e"))
          }
      )
  }
  case class SinglePrompt(value: String) extends Prompt
  case class MultiplePrompt(values: Seq[String]) extends Prompt
}
