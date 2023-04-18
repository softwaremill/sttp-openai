package sttp.openai

import sttp.client4._
import sttp.openai.requests.images.ImageCreationRequestBody.ImageCreationBody

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val imageRequestBody: ImageCreationBody = ImageCreationBody(
    prompt = "A world of warcraft character"
  )

  val req = openAi.createImage(imageRequestBody)

  val res = req.send(backend)
  println(res)
}
