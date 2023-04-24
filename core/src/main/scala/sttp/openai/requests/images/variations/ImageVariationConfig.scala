package sttp.openai.requests.images.variations

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File
import java.nio.file.Paths

case class ImageVariationConfig(
    image: File,
    n: Option[Int] = None,
    size: Option[Size] = None,
    responseFormat: Option[ResponseFormat] = None,
    user: Option[String] = None
)

object ImageVariationConfig {
  def createImageVariationConfigWithSystemPaths(
      systemPathImage: String,
      n: Option[Int],
      size: Option[Size],
      responseFormat: Option[ResponseFormat],
      user: Option[String]
  ): ImageVariationConfig =
    ImageVariationConfig(
      Paths.get(systemPathImage).toFile,
      n,
      size,
      responseFormat,
      user
    )
}
