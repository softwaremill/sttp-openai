package sttp.openai.client

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client4._
import sttp.model.StatusCode
import sttp.model.StatusCode._
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.OpenAIExceptions.OpenAIException._
import sttp.openai.OpenAISyncClient
import sttp.openai.requests.models.ModelsResponseData._

class SyncClientSpec extends AnyFlatSpec with Matchers with EitherValues {
  private val (message, typ, param, code) = ("Some error message.", "error_type", "null", "invalid_api_key")
  private val errorResponse =
    s"""
      |{
      |  "error": {
      |    "message": "$message",
      |    "type": "$typ",
      |    "param": $param,
      |    "code": "$code"
      |  }
      |}""".stripMargin
  private val testData: Seq[(StatusCode, OpenAIException)] = List(
    (TooManyRequests, new RateLimitException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, TooManyRequests))),
    (BadRequest, new InvalidRequestException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, BadRequest))),
    (NotFound, new InvalidRequestException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, NotFound))),
    (
      UnsupportedMediaType,
      new InvalidRequestException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, UnsupportedMediaType))
    ),
    (Unauthorized, new AuthenticationException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, Unauthorized))),
    (Forbidden, new PermissionException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, Forbidden))),
    (Conflict, new TryAgain(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, Conflict))),
    (
      ServiceUnavailable,
      new ServiceUnavailableException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, ServiceUnavailable))
    ),
    (Gone, new APIException(Some(message), Some(typ), None, Some(code), HttpError(errorResponse, Gone)))
  )
  for ((statusCode, expectedError) <- testData)
    s"Service response with status code: $statusCode" should s"return properly deserialized ${expectedError.getClass.getSimpleName}" in {
      val syncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondWithCode(statusCode, errorResponse)
      val syncClient = OpenAISyncClient(authToken = "test-token", backend = syncBackendStub)
      val caught = intercept[OpenAIException](syncClient.getModels)
      caught.getClass shouldBe expectedError.getClass
      caught.message shouldBe expectedError.message
      caught.cause shouldBe expectedError.cause
      caught.code shouldBe expectedError.code
      caught.param shouldBe expectedError.param
      caught.`type` shouldBe expectedError.`type`
    }

  "Fetching models with successful response" should "return properly deserialized list of available models" in {
    val modelsResponse = sttp.openai.fixtures.ModelsGetResponse.singleModelResponse
    val syncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondWithCode(Ok, modelsResponse)
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
    syncClient.getModels shouldBe deserializedModels
  }
}
