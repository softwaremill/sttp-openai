package sttp.openai

import sttp.client4._
import sttp.openai.models.requests.ModelsGetResponseData.ModelsResponse

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openApi: OpenAi = new OpenAi("test")
  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApi.getModels
      .send(backend)

  println(response.code)
  println(response.body)

}
