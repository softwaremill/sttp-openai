package sttp.openai

import sttp.client4._
import sttp.openai.requests.completions.CompletionsRequestBody.{CompletionBody, Prompt}
//import sttp.openai.requests.completions.CompletionsRequestBody.CompletionBody

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("test")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val completionBody = CompletionBody("text-davinci-003", prompt = Some(Prompt.SinglePrompt("Say this is a test")), maxTokens = Some(7), temperature = Some(0))
//  val completionBody = CompletionBody("text-davinci-003", prompt = Some("Say this is a test"), maxTokens = Some(7), temperature = Some(0))

  val response = openAi.createCompletion(completionBody)
  println(response.body)

  val x = response.send(backend)

  println(x.code)
  println(x.body)

}
