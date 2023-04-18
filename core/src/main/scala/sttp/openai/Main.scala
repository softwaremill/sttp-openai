package sttp.openai

import sttp.client4._
import sttp.openai.requests.completions.chat.ChatRequestBody._
import sttp.openai.requests.completions.chat.Message

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val messages = Seq(
    Message(
      role = "user",
      content = "Hello!"
    )
  )

  val chatBody: ChatBody = ChatBody(
    model = "gpt-3.5-turbo",
    messages = messages
  )

//  val resTwo = openAi.createChatCompletionTest(chatBody).send(backend)
//  println(resTwo)

  val response = openAi.createChatCompletion(chatBody).send(backend)

  println(response.body)
}
