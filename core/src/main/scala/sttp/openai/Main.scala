package sttp.openai

import sttp.client4._
import sttp.openai.requests.completions.edit.EditRequestBody.EditBody

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val body: EditBody = EditBody(
    model = "text-davinci-edit-001",
//    input = Some("What day of the wek is it?"),
    instruction = "Fix the spelling mistakes"
  )

  val response = openAi.createEdit(body).send(backend)

  println(response.body)

}
