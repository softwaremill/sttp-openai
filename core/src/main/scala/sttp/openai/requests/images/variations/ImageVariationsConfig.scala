package sttp.openai.requests.images.variations

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File
import java.nio.file.Paths

case class ImageVariationsConfig(
    image: File,
    n: Option[Int] = None,
    size: Option[Size] = None,
    responseFormat: Option[ResponseFormat] = None,
    user: Option[String] = None
)

object ImageVariationsConfig {
  def createImageVariationConfigWithSystemPaths(
      systemPathImage: String,
      n: Option[Int],
      size: Option[Size],
      responseFormat: Option[ResponseFormat],
      user: Option[String]
  ): ImageVariationsConfig =
    ImageVariationsConfig(
      Paths.get(systemPathImage).toFile,
      n,
      size,
      responseFormat,
      user
    )
}
