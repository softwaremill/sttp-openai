package sttp.openai

import sttp.client4._
import sttp.openai.requests.images.ImageCreationRequestBody.ImageCreationBody
import sttp.openai.requests.images.ImageCreationRequestBody.Size.Small

object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("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")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val imageRequestBody: ImageCreationBody = ImageCreationBody(
    prompt = "fishman",
    size = Some(Small)
  )

  val req = openAi.createImage(imageRequestBody)

  val res = req.send(backend)
  println(res)
}
