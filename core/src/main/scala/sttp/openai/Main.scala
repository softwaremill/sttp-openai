package sttp.openai

import sttp.client4._
import sttp.openai.requests.models.ModelsGetResponseData.ModelsResponse

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("test")
  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
    openAi.getModels
      .send(backend)

  println(response.code)
  println(response.body)

}
