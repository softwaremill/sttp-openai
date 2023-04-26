package sttp.openai.requests.images.variations

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File
import java.nio.file.Paths

/*
todo: add scaladocs
 *   - image: A file of base image.
 *   - n: An optional integer specifying the number of images to generate.
 *   - size: An optional instance of the Size case class representing the desired size of the output image.
 *   - responseFormat: An optional instance of the ResponseFormat case class representing the desired format of the response.
 *   - user: An optional, unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
 */
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
