package sttp.openai.requests.embeddings

object EmbeddingsRequestBody {
  case class EmbeddingsBody(model: String, input: String, user: Option[String])

  sealed trait EmbeddingsInput
  object EmbeddingsInput {
    case class SingleInput(value: String) extends EmbeddingsInput
    case class MultipleInput(values: Seq[String]) extends EmbeddingsInput
  }

}
