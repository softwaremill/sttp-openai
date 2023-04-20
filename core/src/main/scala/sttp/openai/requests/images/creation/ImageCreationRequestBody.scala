package sttp.openai.requests.images.creation

import sttp.openai.json.SnakePickle
import sttp.openai.requests.images.Size
import sttp.openai.requests.images.ResponseFormat

object ImageCreationRequestBody {

  case class ImageCreationBody(
      prompt: String,
      n: Option[Int] = None,
      size: Option[Size] = None,
      responseFormat: Option[ResponseFormat] = None,
      user: Option[String] = None
  )

  object ImageCreationBody {
    implicit val imageCreationBodyRW: SnakePickle.ReadWriter[ImageCreationBody] = SnakePickle.macroRW[ImageCreationBody]
  }

}
