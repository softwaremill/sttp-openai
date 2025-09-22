package sttp.ai.claude.models

import upickle.default.{macroRW, ReadWriter}

sealed trait ContentBlock {
  def `type`: String
}

object ContentBlock {
  case class TextContent(text: String) extends ContentBlock {
    val `type`: String = "text"
  }

  case class ImageContent(source: ImageSource) extends ContentBlock {
    val `type`: String = "image"
  }

  case class ImageSource(
      `type`: String,
      mediaType: String,
      data: String
  )

  object ImageSource {
    def base64(mediaType: String, data: String): ImageSource = ImageSource(
      `type` = "base64",
      mediaType = mediaType,
      data = data
    )

    implicit val rw: ReadWriter[ImageSource] = macroRW
  }

  implicit val textContentRW: ReadWriter[TextContent] = macroRW
  implicit val imageContentRW: ReadWriter[ImageContent] = macroRW

  implicit val rw: ReadWriter[ContentBlock] = ReadWriter.merge(
    textContentRW,
    imageContentRW
  )
}
