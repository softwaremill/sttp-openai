package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.Stop

object ChatRequestBody {

  case class ChatBody(
      model: String,
      messages: Seq[Message],
      temperature: Option[Double] = None,
      topP: Option[Double] = None,
      n: Option[Int] = None,
      stream: Option[Boolean] = None,
      stop: Option[Stop] = None,
      maxTokens: Option[Int] = None,
      presencePenalty: Option[Double] = None,
      frequencyPenalty: Option[Double] = None,
      logitBias: Option[Map[String, Float]] = None,
      user: Option[String] = None
  )

  object ChatBody {
    implicit val chatRequestRW: SnakePickle.ReadWriter[ChatBody] = SnakePickle.macroRW[ChatBody]
  }

}
