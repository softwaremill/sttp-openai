package sttp.openai.requests.images

import sttp.openai.json.SnakePickle

object ImageCreationRequestBody {

  case class ImageCreationBody(
      prompt: String,
      n: Option[Int] = None,
      size: Option[String] = None,
      responseFormat: Option[String] = None,
      user: Option[String] = None
  )

  object ImageCreationBody {
    implicit val imageCreationBodyRW: SnakePickle.ReadWriter[ImageCreationBody] = SnakePickle.macroRW[ImageCreationBody]
  }

}
