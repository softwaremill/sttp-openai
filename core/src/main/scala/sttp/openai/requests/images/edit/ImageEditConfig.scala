package sttp.openai.requests.images.edit

import java.io.File

object ImageEditConfig {

  case class DefaultConfig(
      mask: File,
      n: Int,
      size: String,
      responseFormat: String
                          )



}
