package sttp.openai

import sttp.client4._
import sttp.model.Uri
import sttp.openai.requests.models.ModelsGetResponseData.ModelsResponse
import sttp.openai.json.SttpUpickleApiExtension.asJsonSnake
import sttp.openai.requests.completions.CompletionsRequestBody.CompletionBody

class OpenAi(authToken: String) {

  /** Fetches all available models from [[https://platform.openai.com/docs/api-reference/models]] */
  def getModels: Request[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.ModelEndpoint)
      .response(asJsonSnake[ModelsResponse])

  def createCompletion(completionBody: CompletionBody) = {
    import sttp.openai.json.SttpUpickleApiExtension.upickleBodySerializerSnake

    openApiAuthRequest
      .post(OpenAIEndpoints.CompletionsEndpoint)
      .body(completionBody)
  }

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIEndpoints {
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"
    val CompletionsEndpoint: Uri = uri"https://api.openai.com/v1/completions"
}
