package sttp.openai.requests.upload

import sttp.openai.json.SnakePickle

/** Represents the request body for uploading a file.
  *
  * @param filename
  *   The name of the file to upload.
  * @param purpose
  *   The intended purpose of the uploaded file.
  * @param bytes
  *   The number of bytes in the file you are uploading.
  * @param mimeType
  *   The MIME type of the file.
  *
  * This must fall within the supported MIME types for your file purpose. See the supported MIME types for assistants and vision.
  */
case class UploadRequestBody(
    filename: String,
    purpose: String,
    bytes: Int,
    mimeType: String
)

object UploadRequestBody {
  implicit val uploadRequestBodyW: SnakePickle.Writer[UploadRequestBody] = SnakePickle.macroW[UploadRequestBody]
}
