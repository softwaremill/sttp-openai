package sttp.openai

import sttp.client4.{DefaultSyncBackend, SyncBackend}
import sttp.openai.requests.models.ModelsResponseData.ModelsResponse

class OpenAISyncClient private (authToken: String, organization: Option[String] = None, backend: SyncBackend) {

  private val openAI = new OpenAI(authToken)
//  def getModels: Either[String, ModelsResponse] =
//    openAI.getModels
//      .send(backend)
//      .body

  // todo: zmienic retrieveFileContent zeby zwracal Either[Error, String] a nie Either [String, String]

  def close(): Unit = backend.close()
}

object OpenAISyncClient {
  def apply(authToken: String) = new OpenAISyncClient(authToken, None, DefaultSyncBackend())
  def apply(authToken: String, backend: SyncBackend) = new OpenAISyncClient(authToken, None, backend)
}
