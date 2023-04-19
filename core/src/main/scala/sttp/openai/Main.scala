package sttp.openai

import sttp.client4._
//import sttp.openai.requests.finetunes.FineTunesRequestBody

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("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")
  val response =
    openAi
//      .createFineTune(FineTunesRequestBody("file-ntUIeQbt4iFIRNOKsyjDoLFT"))
      .getFineTunes
      .send(backend)

  println(response)
}
