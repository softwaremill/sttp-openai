package sttp.openai

import java.io.File
import sttp.client4._
//import sttp.openai.requests.images.Size
//import sttp.openai.requests.images.ResponseFormat
object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val sample_image: File = new File("/Users/adamrybicki/Desktop/sttp-openai/core/src/main/scala/sttp/openai/sample_image.png")
  val sample_mask: File = new File("/Users/adamrybicki/Desktop/sttp-openai/core/src/main/scala/sttp/openai/sample_mask.png")

  val body = sttp.openai.requests.images.edit.ImageEditConfig(sample_image, "put tomato there", Some(sample_mask))

  val response = openAi.imageEdit(body).send(backend)

  println(response)

//  val res = openAi.imageEdit(sample_image, sample_mask, "Put Andrzej Leper in there").send(backend)
//  println(res)

}
