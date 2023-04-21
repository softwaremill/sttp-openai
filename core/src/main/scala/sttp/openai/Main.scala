package sttp.openai

import sttp.client4._

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
  //  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
  //    openAi.getModels
  //      .send(backend)

}