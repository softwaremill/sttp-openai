package sttp.openai

import sttp.client4._
import sttp.openai.models.requests.ModelsGetResponseData.ModelsResponse

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openApi: OpenAi = new OpenAi("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")
  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApi.getModels
      .send(backend)

  println(response.code)
  println(response.body)

}
