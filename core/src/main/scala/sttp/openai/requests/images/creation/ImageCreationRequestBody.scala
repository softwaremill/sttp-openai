package sttp.openai.requests.images.creation

import sttp.openai.json.SnakePickle
import sttp.openai.requests.images.Size
import sttp.openai.requests.images.ResponseFormat

object ImageCreationRequestBody {

  /** @param prompt
    *   A text description of the desired image(s). The maximum length is 1000 characters.
    * @param model
    *   A name of the model to use for image generation
    * @param n
    *   The number of images to generate. Must be between 1 and 10.
    * @param size
    *   The size of the generated images. Must be one of `256x256`, `512x512`, or `1024x1024`.
    * @param responseFormat
    *   The format in which the generated images are returned. Must be one of `url`` or `b64_json`.
    * @param user
    *   A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
    */
  case class ImageCreationBody(
      prompt: String,
      model: String,
      n: Option[Int] = None,
      size: Option[Size] = None,
      responseFormat: Option[ResponseFormat] = None,
      user: Option[String] = None
  )

  object ImageCreationBody {
    implicit val imageCreationBodyW: SnakePickle.Writer[ImageCreationBody] = SnakePickle.macroW[ImageCreationBody]
  }

}
