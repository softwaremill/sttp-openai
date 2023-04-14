package sttp.openai

import sttp.client4._
import sttp.openai.requests.completions.CompletionsRequestBody.{CompletionsBody, MultiplePrompt}

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

//  val completionBody = CompletionBody("text-davinci-003", prompt = Some(SinglePrompt("Say this is a test")), maxTokens = Some(7), temperature = Some(0))
  val compBody = CompletionsBody(model = "text-davinci-003", prompt = Some(MultiplePrompt(Seq("Say this is a test", "xD"))))
//    val compBody = CompBody(model = "text-davinci-003", prompt = Some("Say this is a test"))
//  val completionBody = CompletionBody("text-davinci-003", prompt = Some("Say this is a test"), maxTokens = Some(7), temperature = Some(0))

  println(compBody.prompt)
  val response = openAi.createCompletion(compBody)
  println(response.body)

  val x = response.send(backend)

  println(x.code)
  println(x.body)

}
