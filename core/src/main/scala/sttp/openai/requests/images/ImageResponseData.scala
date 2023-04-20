package sttp.openai.requests.images

import sttp.openai.json.SnakePickle

object ImageResponseData {

  case class ImageResponse(
      created: Int,
      data: Seq[GeneratedImageData]
  )

  object ImageResponse {
    implicit val imageCreationResponseRW: SnakePickle.ReadWriter[ImageResponse] = SnakePickle.macroRW[ImageResponse]
  }

  case class GeneratedImageData(url: String)

  object GeneratedImageData {
    implicit val generatedImageDataRW: SnakePickle.ReadWriter[GeneratedImageData] = SnakePickle.macroRW[GeneratedImageData]
  }

}
