package sttp.openai

import sttp.client4._
import sttp.openai.requests.embeddings.EmbeddingsRequestBody.EmbeddingsInput.SingleInput

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("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")
  val response = // : Response[Either[ResponseException[String, Exception], ModelsResponse]] =
    openAi
      .createEmbeddings(
        sttp.openai.requests.embeddings.EmbeddingsRequestBody.EmbeddingsBody(
          "text-embedding-ada-002",
          SingleInput("The food was delicious and the waiter...")
        )
      )
      .send(backend)

  println(response)

}
