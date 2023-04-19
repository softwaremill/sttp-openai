package sttp.openai

import sttp.client4._
import sttp.openai.requests.finetunes.FineTunesRequestBody

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
  val response =
    openAi
      .createFineTune(FineTunesRequestBody("file-ntUIeQbt4iFIRNOKsyjDoLFT"))
      .send(backend)

  println(response)
}
