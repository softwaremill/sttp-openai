package sttp.openai

import java.io.File
import sttp.client4._
import .ImageEditBody
object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openAi: OpenAi = new OpenAi("sk-")
//  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] =
//    openAi.getModels
//      .send(backend)

  val imageEditBody: ImageEditBody = ImageEditBody("test")
  val file: File = new File("sttp/openai/resources/Screenshot 2023-02-02 at 08.40.24.png")
  val p = openAi.imageEdit(file, imageEditBody)

  println(p)

}
