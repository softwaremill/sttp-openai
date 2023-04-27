package sttp.openai.requests.images.edit

import sttp.openai.requests.images.{ResponseFormat, Size}

import java.io.File
import java.nio.file.Paths

/** @param image
  *   The image to edit. Must be a valid PNG file, less than 4MB, and square. If mask is not provided, image must have transparency, which
  *   will be used as the mask.
  * @param prompt
  *   A text description of the desired image(s). The maximum length is 1000 characters.
  * @param mask
  *   An additional image whose fully transparent areas (e.g. where alpha is zero) indicate where image should be edited. Must be a valid
  *   PNG file, less than 4MB, and have the same dimensions as image.
  * @param n
  *   The number of images to generate. Must be between 1 and 10.
  * @param size
  *   The size of the generated images. Must be one of `256x256`, `512x512`, or `1024x1024`.
  * @param responseFormat
  *   The format in which the generated images are returned. Must be one of `url`` or `b64_json`.
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
