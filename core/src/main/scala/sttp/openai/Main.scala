package sttp.openai

import sttp.client4._
import sttp.openai.requests.completions.CompletionsRequestBody.{CompletionsBody, MultiplePrompt}

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val compBody = CompletionsBody(model = "text-davinci-003", prompt = Some(MultiplePrompt(Seq("Say this is a test", "Say this is a test2"))))

  println(compBody.prompt)
  val response = openAi.createCompletion(compBody)
  println(response.body)

  val x = response.send(backend)

  println(x.code)
  println(x.body)

}
