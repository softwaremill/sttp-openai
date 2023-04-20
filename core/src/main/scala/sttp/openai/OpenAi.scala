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
import sttp.openai.requests.files.FilesResponseData.{FileData, FilesResponse}
import sttp.openai.requests.finetunes.FineTunesRequestBody
import sttp.openai.requests.finetunes.FineTunesResponseData.{CreateFineTuneResponse, GetFineTunesResponse}
import sttp.openai.requests.models.ModelsResponseData.{ModelData, ModelsResponse}

class OpenAi(authToken: String) {

  /** Fetches all available models from [[https://platform.openai.com/docs/api-reference/models]] */
  def getModels: Request[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.ModelEndpoint)
      .response(asJsonSnake[ModelsResponse])

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

  /** @param fileId
    *   The ID of the file to use for this request.
    * @return
    *   Returns information about a specific file.
    */
  def retrieveFile(fileId: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.retrieveFileEndpoint(fileId))
      .response(asJsonSnake[FileData])

  /** Creates a job that fine-tunes a specified model from a given dataset.
    * @param fineTunesRequestBody
    *   Request body that will be used to create a fine-tune.
    * @return
    *   Details of the enqueued job including job status and the name of the fine-tuned models once complete.
    */
  def createFineTune(
      fineTunesRequestBody: FineTunesRequestBody
  ): Request[Either[ResponseException[String, Exception], CreateFineTuneResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.FineTunesEndpoint)
      .body(fineTunesRequestBody)
      .response(asJsonSnake[CreateFineTuneResponse])

  /** @return
    *   List of your organization's fine-tuning jobs.
    */
  def getFineTunes: Request[Either[ResponseException[String, Exception], GetFineTunesResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.FineTunesEndpoint)
      .response(asJsonSnake[GetFineTunesResponse])

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIEndpoints {
  val ChatEndpoint: Uri = uri"https://api.openai.com/v1/chat/completions"
  val CompletionsEndpoint: Uri = uri"https://api.openai.com/v1/completions"
  val EditEndpoint: Uri = uri"https://api.openai.com/v1/edits"
  val FilesEndpoint: Uri = uri"https://api.openai.com/v1/files"
  val FineTunesEndpoint: Uri = uri"https://api.openai.com/v1/fine-tunes"
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"

  def retrieveFileEndpoint(fileId: String): Uri = FilesEndpoint.addPath(fileId)
  def retrieveModelEndpoint(modelId: String): Uri = ModelEndpoint.addPath(modelId)
}
