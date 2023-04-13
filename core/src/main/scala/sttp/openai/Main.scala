package sttp.openai

import sttp.client4._

//import sttp.openai.requests.models.ModelsGetResponseData.ModelsResponse

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("test")

  val response = openAi.retrieveModel("text-davinci-003").send(backend)

  println(response.code)
  println(response.body)

}
