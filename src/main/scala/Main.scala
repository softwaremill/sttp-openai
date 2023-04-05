import requests.models.ModelsGetResponseData.ModelsResponse
import sttp.client4._


object Main extends App {
  val backend: SyncBackend = DefaultSyncBackend()

  val openApi: OpenAI = new OpenAI("test")
  val response: Response[Either[ResponseException[String, Exception], ModelsResponse]] = openApi.getModels.send(backend)

  println(response.code)
  println(response.body)

}
