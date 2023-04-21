package sttp.openai.requests.images

import sttp.openai.json.SnakePickle

object ImageCreationResponseData {

  case class ImageCreationResponse(
      created: Int,
      data: Seq[GeneratedImageData]
  )

  object ImageCreationResponse {
    implicit val imageCreationResponseRW: SnakePickle.ReadWriter[ImageCreationResponse] = SnakePickle.macroRW[ImageCreationResponse]
  }

  case class GeneratedImageData(url: String)

  object GeneratedImageData {
    implicit val generatedImageDataRW: SnakePickle.ReadWriter[GeneratedImageData] = SnakePickle.macroRW[GeneratedImageData]
  }

}
