package sttp.openai.requests.completions

import sttp.openai.json.SnakePickle

object CompletionsRequestBody {
  case class CompletionBody(
      model: String,
      @upickle.implicits.key("prompt") prompt: Option[Prompt] = None,
//      prompt: Option[String] = None,
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

  object CompletionBody {
    implicit def completionBodyRW: SnakePickle.ReadWriter[CompletionBody] = SnakePickle.macroRW[CompletionBody]
  }

  sealed trait Prompt

  object Prompt {

    implicit def rw: SnakePickle.ReadWriter[Prompt] = SnakePickle.ReadWriter.merge(
//      SinglePrompt.singlePromptRW,
//      MultiplePrompt.multiplePromptRW
      SnakePickle.macroRW[SinglePrompt],
      SnakePickle.macroRW[MultiplePrompt]
    )

  }

  upickle.implicits.key("prompt")
  case class SinglePrompt(value: String) extends Prompt
  object SinglePrompt {
    implicit def singlePromptRW: SnakePickle.ReadWriter[SinglePrompt] = SnakePickle.macroRW[SinglePrompt]
  }

  case class MultiplePrompt(values: Seq[String]) extends Prompt

  object MultiplePrompt {
    implicit def multiplePromptRW: SnakePickle.ReadWriter[MultiplePrompt] = SnakePickle.macroRW[MultiplePrompt]
  }

  sealed trait Stop

  object Stop {
    implicit val stopRW: SnakePickle.ReadWriter[Stop] = SnakePickle.ReadWriter.merge(
      SnakePickle.macroRW[SingleStop],
      SnakePickle.macroRW[MultipleStop]
    )

    case class SingleStop(value: String) extends Stop
//    object SingleStop {
//      implicit def singleStopRW: SnakePickle.ReadWriter[SingleStop] = SnakePickle.macroRW[SingleStop]
//    }
    case class MultipleStop(values: Seq[String]) extends Stop
//    object MultipleStop {
//      implicit def multipleStopRW: SnakePickle.ReadWriter[MultipleStop] = SnakePickle.macroRW[MultipleStop]
//    }
  }

}
