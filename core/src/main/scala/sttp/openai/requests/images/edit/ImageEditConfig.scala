package sttp.openai.requests.images.edit

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File
import java.nio.file.Paths
case class ImageEditConfig(
    image: File,
    prompt: String,
    mask: Option[File] = None,
    n: Option[Int] = None,
    size: Option[Size] = None,
    responseFormat: Option[ResponseFormat] = None
)

object ImageEditConfig {
  def createImageEditConfigWithSystemPaths(
      systemPathImage: String,
      prompt: String,
      systemPathMask: Option[String],
      n: Option[Int],
      size: Option[Size],
      responseFormat: Option[ResponseFormat]
  ): ImageEditConfig = {
    val image: File = Paths.get(systemPathImage).toFile
    val mask: Option[File] = systemPathMask.map(Paths.get(_).toFile)

    ImageEditConfig(image, prompt, mask, n, size, responseFormat)
  }
}
