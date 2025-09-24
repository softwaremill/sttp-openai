package sttp.ai.claude.models

import upickle.default.{macroRW, ReadWriter}
import ujson.Value

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

  case class ToolUseContent(
      id: String,
      name: String,
      input: Map[String, Value]
  ) extends ContentBlock {
    val `type`: String = "tool_use"
  }

  case class ToolResultContent(
      toolUseId: String,
      content: String,
      isError: Option[Boolean] = None
  ) extends ContentBlock {
    val `type`: String = "tool_result"
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
  implicit val toolUseContentRW: ReadWriter[ToolUseContent] = macroRW
  implicit val toolResultContentRW: ReadWriter[ToolResultContent] = macroRW

  implicit val rw: ReadWriter[ContentBlock] = ReadWriter.merge(
    textContentRW,
    imageContentRW,
    toolUseContentRW,
    toolResultContentRW
  )
}
