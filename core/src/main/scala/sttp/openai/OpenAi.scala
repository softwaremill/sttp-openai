package sttp.openai

import sttp.client4._
import sttp.model.Uri
import sttp.openai.requests.models.ModelsResponseData.{ModelData, ModelsResponse}
import sttp.openai.json.SttpUpickleApiExtension.asJsonSnake

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
      .get(OpenAIEndpoints.RetrieveModelEndpoint(modelId))
      .response(asJsonSnake[ModelData])

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIEndpoints {
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"
  def RetrieveModelEndpoint(modelId: String): Uri = ModelEndpoint.addPath(modelId)
}
