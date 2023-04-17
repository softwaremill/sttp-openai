package sttp.openai

import sttp.client4._
import sttp.model.Uri
import sttp.openai.requests.models.ModelsGetResponseData.ModelsResponse
import sttp.openai.json.SttpUpickleApiExtension.asJsonSnake
import sttp.openai.requests.files.FilesResponseData._

class OpenAi(authToken: String) {

  /** Fetches all available models from [[https://platform.openai.com/docs/api-reference/models]] */
  def getModels: Request[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.ModelEndpoint)
      .response(asJsonSnake[ModelsResponse])

  /** Fetches all files that belong to the user's organization from [[https://platform.openai.com/docs/api-reference/files]] */
  def getFiles: Request[Either[ResponseException[String, Exception], FilesResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.FilesEndpoint)
      .response(asJsonSnake[FilesResponse])

  /** @param fileId
    *   The ID of the file to use for this request.
    * @return
    *   Returns information about a specific file.
    */
  def retrieveFile(fileId: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.retrieveFileEndpoint(fileId))
      .response(asJsonSnake[FileData])

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIEndpoints {
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"
  val FilesEndpoint: Uri = uri"https://api.openai.com/v1/files"
  def retrieveFileEndpoint(fileId: String): Uri = FilesEndpoint.addPath(fileId)
}
