package sttp.openai

import sttp.client4._
import sttp.model.Uri
import sttp.openai.json.SttpUpickleApiExtension.{asJsonSnake, upickleBodySerializerSnake}
import sttp.openai.requests.completions.CompletionsRequestBody.CompletionsBody
import sttp.openai.requests.completions.CompletionsResponseData.CompletionsResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.edit.EditRequestBody.EditBody
import sttp.openai.requests.completions.edit.EditRequestResponseData.EditResponse
import sttp.openai.requests.files.FilesResponseData._
import sttp.openai.requests.models.ModelsResponseData.{ModelData, ModelsResponse}

import java.io.File
import java.nio.file.Paths

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

  /** @param completionBody
    *   Request body
    *
    * Creates a completion for the provided prompt and parameters given in request body and send it over to
    * [[https://api.openai.com/v1/completions]]
    */
  def createCompletion(completionBody: CompletionsBody): Request[Either[ResponseException[String, Exception], CompletionsResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.CompletionsEndpoint)
      .body(completionBody)
      .response(asJsonSnake[CompletionsResponse])

  /** @param editRequestBody
    *   Edit request body
    *
    * Creates a new edit for provided request body and send it over to [[https://api.openai.com/v1/chat/completions]]
    */
  def createEdit(editRequestBody: EditBody): Request[Either[ResponseException[String, Exception], EditResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.EditEndpoint)
      .body(editRequestBody)
      .response(asJsonSnake[EditResponse])

  /** @param chatBody
    *   Chat request body
    *
    * Creates a completion for the chat message given in request body and send it over to [[https://api.openai.com/v1/chat/completions]]
    */
  def createChatCompletion(chatBody: ChatBody): Request[Either[ResponseException[String, Exception], ChatResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.ChatEndpoint)
      .body(chatBody)
      .response(asJsonSnake[ChatResponse])

  /** Fetches all files that belong to the user's organization from [[https://platform.openai.com/docs/api-reference/files]] */
  def getFiles: Request[Either[ResponseException[String, Exception], FilesResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.FilesEndpoint)
      .response(asJsonSnake[FilesResponse])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    * @param file
    *   [[java.io.File File]] of the JSON Lines file to be uploaded. <p> If the purpose is set to "fine-tune", each line is a JSON record
    *   with "prompt" and "completion" fields representing your
    *   [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @param purpose
    *   The intended purpose of the uploaded documents. <p> Use "fine-tune" for Fine-tuning. This allows OpenAI to validate the format of
    *   the uploaded file.
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(file: File, purpose: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.FilesEndpoint)
      .multipartBody(
        multipart("purpose", purpose),
        multipartFile("file", file)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * @param file
    *   [[java.io.File File]] of the JSON Lines file to be uploaded and the purpose is set to "fine-tune", each line is a JSON record with
    *   "prompt" and "completion" fields representing your
    *   [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(file: File): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.FilesEndpoint)
      .multipartBody(
        multipart("purpose", "fine-tune"),
        multipartFile("file", file)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * @param systemPath
    *   [[java.lang.String systemPath]] of the JSON Lines file to be uploaded. <p> If the purpose is set to "fine-tune", each line is a JSON
    *   record with "prompt" and "completion" fields representing your
    *   [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @param purpose
    *   The intended purpose of the uploaded documents. <p> Use "fine-tune" for Fine-tuning. This allows OpenAI to validate the format of
    *   the uploaded file.
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(systemPath: String, purpose: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.FilesEndpoint)
      .multipartBody(
        multipart("purpose", purpose),
        multipartFile("file", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * @param systemPath
    *   [[java.lang.String systemPath]] of the JSON Lines file to be uploaded and the purpose is set to "fine-tune", each line is a JSON
    *   record with "prompt" and "completion" fields representing your
    *   [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(systemPath: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.FilesEndpoint)
      .multipartBody(
        multipart("purpose", "fine-tune"),
        multipartFile("file", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[FileData])

  /** @param fileId
    *   The ID of the file to use for this request.
    * @return
    *   Information about deleted file.
    */
  def deleteFile(fileId: String): Request[Either[ResponseException[String, Exception], DeletedFileData]] =
    openApiAuthRequest
      .delete(OpenAIEndpoints.deleteFileEndpoint(fileId))
      .response(asJsonSnake[DeletedFileData])

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
  val ChatEndpoint: Uri = uri"https://api.openai.com/v1/chat/completions"
  val CompletionsEndpoint: Uri = uri"https://api.openai.com/v1/completions"
  val EditEndpoint: Uri = uri"https://api.openai.com/v1/edits"
  val FilesEndpoint: Uri = uri"https://api.openai.com/v1/files"
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"
  def deleteFileEndpoint(fileId: String): Uri = FilesEndpoint.addPath(fileId)
  def retrieveFileEndpoint(fileId: String): Uri = FilesEndpoint.addPath(fileId)
  def retrieveModelEndpoint(modelId: String): Uri = ModelEndpoint.addPath(modelId)
}
