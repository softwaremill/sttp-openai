package sttp.openai.requests.images.edit

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File
import java.nio.file.Paths
case class ImageEditsConfig(
    image: File,
    prompt: String,
    mask: Option[File] = None,
    n: Option[Int] = None,
    size: Option[Size] = None,
    responseFormat: Option[ResponseFormat] = None
)

object ImageEditsConfig {
  def createImageEditConfigWithSystemPaths(
      systemPathImage: String,
      prompt: String,
      systemPathMask: Option[String],
      n: Option[Int],
      size: Option[Size],
      responseFormat: Option[ResponseFormat]
  ): ImageEditsConfig = {
    val image: File = Paths.get(systemPathImage).toFile
    val mask: Option[File] = systemPathMask.map(Paths.get(_).toFile)

    ImageEditsConfig(image, prompt, mask, n, size, responseFormat)
  }
}
