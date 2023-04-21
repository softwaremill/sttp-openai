package sttp.openai.requests.embeddings

import sttp.openai.json.SnakePickle

object EmbeddingsRequestBody {

  /** @param model
    *   ID of the model to use.
    * @param input
    *   Input text to get embeddings for, encoded as a string or array of tokens.
    * @param user
    *   A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
    */
  case class EmbeddingsBody(model: String, input: EmbeddingsInput, user: Option[String] = None)

  object EmbeddingsBody {
    implicit val embeddingsBodyWriter: SnakePickle.Writer[EmbeddingsBody] = SnakePickle.macroW[EmbeddingsBody]
  }

  sealed trait EmbeddingsInput
  object EmbeddingsInput {
    case class SingleInput(value: String) extends EmbeddingsInput
    case class MultipleInput(values: Seq[String]) extends EmbeddingsInput

    implicit val embeddingsInputWriter: SnakePickle.Writer[EmbeddingsInput] = SnakePickle.writer[ujson.Value].comap[EmbeddingsInput] {
      case SingleInput(value)    => SnakePickle.writeJs(value)
      case MultipleInput(values) => SnakePickle.writeJs(values)
    }
  }
}
