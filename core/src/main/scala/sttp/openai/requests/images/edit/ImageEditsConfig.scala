package sttp.openai.requests.images.edit

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File
import java.nio.file.Paths

/* todo: add scaladocs
 *   - image: A file representing the image to be edited.
 *   - prompt: A string describing the desired edits to be made to the image.
 *   - mask: An optional file representing a mask to be applied to the image.
 *   - n: An optional integer specifying the number of edits to be made.
 *   - size: An optional instance of the Size case class representing the desired size of the output image.
 *   - responseFormat: An optional instance of the ResponseFormat case class representing the desired format of the response.
 */
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
