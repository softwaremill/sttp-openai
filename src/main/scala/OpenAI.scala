import requests.models.ModelsGetResponseData.ModelsResponse
import sttp.client4._
import sttp.client4.jsoniter._
import sttp.model.Uri

class OpenAI(val authToken: String) {

  def getModels: Request[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.ModelEndpoint)
      .response(asJson[ModelsResponse])

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIEndpoints {
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"

}
