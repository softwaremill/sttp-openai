package sttp.openai.client

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client4._
import sttp.model.StatusCode._
import sttp.client4.testing._
import sttp.openai.OpenAIErrors.OpenAIError._
import sttp.openai.OpenAISyncClient
import sttp.openai.requests.models.ModelsResponseData._

class SyncClientSpec extends AnyFlatSpec with Matchers with EitherValues {
  private val testData = List(
    (TooManyRequests, classOf[RateLimitError]),
    (BadRequest, classOf[InvalidRequestError]),
    (NotFound, classOf[InvalidRequestError]),
    (UnsupportedMediaType, classOf[InvalidRequestError]),
    (Unauthorized, classOf[AuthenticationError]),
    (Forbidden, classOf[PermissionError]),
    (Conflict, classOf[TryAgain]),
    (ServiceUnavailable, classOf[ServiceUnavailableError]),
    (Gone, classOf[APIError])
  )
  private val errorResponse =
    """
      |{
      |  "error": {
      |    "message": "Some error message.",
      |    "type": "error_type",
      |    "param": null,
      |    "code": "invalid_api_key"
      |  }
      |}""".stripMargin

  for ((statusCode, expectedError) <- testData)
    s"Service response with status code: $statusCode" should s"return ${expectedError.getSimpleName}" in {
      val syncBackendStub: SyncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondWithCode(statusCode, errorResponse)
      val syncClient = OpenAISyncClient(authToken = "test-token", backend = syncBackendStub)
      syncClient.getModels.left.value.getClass.getName shouldBe expectedError.getName
    }

  "Fetching models with successful response" should "return properly deserialized list of available models" in {
    val modelsResponse = sttp.openai.fixtures.ModelsGetResponse.singleModelResponse
    val syncBackendStub: SyncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondWithCode(Ok, modelsResponse)
    val syncClient = OpenAISyncClient(authToken = "test-token", backend = syncBackendStub)
    val deserializedModels = ModelsResponse(
      `object` = "list",
      data = Seq(
        ModelData(
          id = "babbage",
          `object` = "model",
          created = 1649358449,
          ownedBy = "openai",
          permission = Seq(
            ModelPermission(
              id = "modelperm-49FUp5v084tBB49tC4z8LPH5",
              `object` = "model_permission",
              created = 1669085501,
              allowCreateEngine = false,
              allowSampling = true,
              allowLogprobs = true,
              allowSearchIndices = false,
              allowView = true,
              allowFineTuning = false,
              organization = "*",
              group = None,
              isBlocking = false
            )
          ),
          root = "babbage",
          parent = None
        )
      )
    )
    syncClient.getModels.value shouldBe deserializedModels
  }
}
