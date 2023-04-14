package sttp.openai

import sttp.client4._
//import sttp.openai.requests.completions.CompletionsRequestBody.CompletionBody
import sttp.openai.requests.completions.CompReqBody.CompBody
import sttp.openai.requests.completions.CompReqBody.SinglePrompt
//import sttp.openai.requests.completions.CompletionsRequestBody.SinglePrompt
//import sttp.openai.requests.completions.CompletionsRequestBody.CompletionBody

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("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")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

//  val completionBody = CompletionBody("text-davinci-003", prompt = Some(SinglePrompt("Say this is a test")), maxTokens = Some(7), temperature = Some(0))
    val compBody = CompBody(model = "text-davinci-003", prompt = Some(SinglePrompt("Say this is a test")))
//    val compBody = CompBody(model = "text-davinci-003", prompt = Some("Say this is a test"))
//  val completionBody = CompletionBody("text-davinci-003", prompt = Some("Say this is a test"), maxTokens = Some(7), temperature = Some(0))

  println(compBody.prompt)
  val response = openAi.createCompletion(compBody)
  println(response.body)

  val x = response.send(backend)

  println(x.code)
  println(x.body)

}
