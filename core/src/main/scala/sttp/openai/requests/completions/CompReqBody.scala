package sttp.openai.requests.completions

import upickle.default.{macroRW, ReadWriter}

object CompReqBody {

  case class CompBody(
      model: String,
      prompt: Option[Prompt] = None,
//            prompt: Option[String] = None,
      suffix: Option[String] = None,
      maxTokens: Option[Int] = None,
      temperature: Option[Double] = None,
      top_p: Option[Double] = None,
      n: Option[Int] = None,
      stream: Option[Boolean] = None,
      logprobs: Option[Int] = None,
      echo: Option[Boolean] = None,
      stop: Option[Stop] = None,
      presence_penalty: Option[Double] = None,
      frequency_penalty: Option[Double] = None,
      best_of: Option[Int] = None,
      logit_bias: Option[Map[String, Float]] = None,
      user: Option[String] = None
  )

  object CompBody {
    implicit val completionBodyRW: ReadWriter[CompBody] = macroRW[CompBody]
  }

  sealed trait Prompt
  object Prompt {
    implicit val promptRW: ReadWriter[Prompt] = upickle.default.readwriter[String].bimap[Prompt](_ match {
      case SinglePrompt(value) => value
      case MultiplePrompt(values) => values.toString
    }, s => SinglePrompt(s))
//    implicit val promptRW: ReadWriter[Prompt] = upickle.default.macroRW[Prompt].bimap[String](s => SinglePrompt(s), prompt => prompt match {
//      case SinglePrompt(value) => value
//      case MultiplePrompt(values) => values.toString()
//    })

//    implicit val promptWriter: Writer[Prompt] = (prompt: Prompt) => {
//      upickle.Js.Obj("value" -> upickle.default.writeJs(prompt))
//    }

//    implicit val promptReader: Reader[Prompt] = upickle.default.macroR
  }
  case class SinglePrompt(value: String) extends Prompt
  object SinglePrompt {
    implicit val singlePromptRW: ReadWriter[SinglePrompt] = upickle.default.stringKeyRW(upickle.default.readwriter[String].bimap[SinglePrompt](_.value, SinglePrompt(_)))
  }
  case class MultiplePrompt(values: Seq[String]) extends Prompt
  object MultiplePrompt {
    implicit val multiplePromptRW: ReadWriter[MultiplePrompt] = upickle.default.stringKeyRW(upickle.default.readwriter[Seq[String]].bimap[MultiplePrompt](_.values, MultiplePrompt(_)))
  }


  sealed trait Stop
  object Stop {
    implicit val stopRW: ReadWriter[Stop] = ReadWriter.merge(
      macroRW[SingleStop],
      macroRW[MultipleStop]
    )
  }
  case class SingleStop(value: String) extends Stop
  case class MultipleStop(values: Seq[String]) extends Stop
}
