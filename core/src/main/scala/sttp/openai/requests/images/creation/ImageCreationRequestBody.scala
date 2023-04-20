package sttp.openai.requests.images.creation

import sttp.openai.json.SnakePickle
import sttp.openai.requests.images.Size
import sttp.openai.requests.images.ResponseFormat

object ImageCreationRequestBody {

  case class ImageCreationBody(
      prompt: String,
      n: Option[Int] = None,
      size: Option[String] = None,
      responseFormat: Option[String] = None,
      user: Option[String] = None
  )

  object ImageCreationBody {
    def create(
        prompt: String,
        size: Size,
        responseFormat: ResponseFormat,
        n: Option[Int] = None,
        user: Option[String] = None
    ): ImageCreationBody = ImageCreationBody(
      prompt = prompt,
      n = n,
      size = Some(size.value),
      responseFormat = Some(responseFormat.value),
      user = user
    )

    implicit val imageCreationBodyRW: SnakePickle.ReadWriter[ImageCreationBody] = SnakePickle.macroRW[ImageCreationBody]
  }

}
