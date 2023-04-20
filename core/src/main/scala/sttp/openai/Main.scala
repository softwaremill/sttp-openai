package sttp.openai

import java.io.File
import sttp.client4._
import sttp.openai.requests.images.Size
import sttp.openai.requests.images.ResponseFormat
object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val sample_image: File = new File("/Users/adamrybicki/Desktop/sttp-openai/core/src/main/scala/sttp/openai/sample_image.png")
  val sample_mask: File = new File("/Users/adamrybicki/Desktop/sttp-openai/core/src/main/scala/sttp/openai/sample_mask.png")
  println(ResponseFormat.URL.toString)
  println(openAi.imageEdit(sample_image, "random", Some(sample_mask), None, Some(Size.Large), Some(ResponseFormat.URL)))
//  val res = openAi.imageEdit(sample_image, sample_mask, "Put Andrzej Leper in there").send(backend)
//  println(res)

}
