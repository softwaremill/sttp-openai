package sttp.openai

import sttp.client4._
import sttp.model.Uri
import sttp.openai.requests.models.ModelsResponseData.{ModelData, ModelsResponse}
import sttp.openai.json.SttpUpickleApiExtension.asJsonSnake
import sttp.openai.requests.files.FilesResponseData._

class OpenAi(authToken: String) {

  /** Fetches all available models from [[https://platform.openai.com/docs/api-reference/models]] */
  def getModels: Request[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.ModelEndpoint)
      .response(asJsonSnake[ModelsResponse])

  /** @param modelId
    *   a Model's Id as String
    *
    * Fetches an available model for given modelId from [[https://platform.openai.com/docs/api-reference/models/{modelId}]]
    */
  def retrieveModel(modelId: String): Request[Either[ResponseException[String, Exception], ModelData]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.retrieveModelEndpoint(modelId))
      .response(asJsonSnake[ModelData])

  /** Fetches all files that belong to the user's organization from [[https://platform.openai.com/docs/api-reference/files]] */
  def getFiles: Request[Either[ResponseException[String, Exception], FilesResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.FilesEndpoint)
      .response(asJsonSnake[FilesResponse])

  /** @param fileId
    *   The ID of the file to use for this request.
    * @return
    *   Information about deleted file.
    */
  def deleteFile(fileId: String): Request[Either[ResponseException[String, Exception], DeletedFileData]] =
    openApiAuthRequest
      .delete(OpenAIEndpoints.deleteFileEndpoint(fileId))
      .response(asJsonSnake[DeletedFileData])

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIEndpoints {
  val FilesEndpoint: Uri = uri"https://api.openai.com/v1/files"
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"
  def retrieveModelEndpoint(modelId: String): Uri = ModelEndpoint.addPath(modelId)
  def deleteFileEndpoint(fileId: String): Uri = FilesEndpoint.addPath(fileId)
}
