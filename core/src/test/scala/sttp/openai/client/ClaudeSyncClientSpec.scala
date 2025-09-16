package sttp.openai.client

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client4._
import sttp.model.StatusCode._
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.fixtures.{ClaudeFixture, ErrorFixture}
import sttp.openai.{ClaudeSyncClient}

import java.util.concurrent.atomic.AtomicReference

class ClaudeSyncClientSpec extends AnyFlatSpec with Matchers with EitherValues {

  for ((statusCode, expectedError) <- ErrorFixture.testData)
    s"Service response with status code: $statusCode" should s"return properly deserialized ${expectedError.getClass.getSimpleName}" in {
      // given
      val syncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondAdjust(ErrorFixture.errorResponse, statusCode)
      val claudeClient = ClaudeSyncClient(apiKey = "test-api-key", backend = syncBackendStub)

      // when
      val caught = intercept[OpenAIException](claudeClient.createMessage(ClaudeFixture.sampleMessageRequest))

      // then
      caught.getClass shouldBe expectedError.getClass: Unit
      caught.message shouldBe expectedError.message: Unit
      caught.cause.getClass shouldBe expectedError.cause.getClass: Unit
      caught.code shouldBe expectedError.code: Unit
      caught.param shouldBe expectedError.param: Unit
      caught.`type` shouldBe expectedError.`type`
    }

  "Creating message with successful response" should "return properly deserialized Claude message response" in {
    // given
    val messageResponse = ClaudeFixture.sampleMessageResponseJson
    val syncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondAdjust(messageResponse, Ok)
    val claudeClient = ClaudeSyncClient(apiKey = "test-api-key", backend = syncBackendStub)

    // when
    val result = claudeClient.createMessage(ClaudeFixture.sampleMessageRequest)

    // then
    result shouldBe ClaudeFixture.sampleMessageResponse
  }

  "ClaudeSyncClient" should "use correct headers for authentication" in {
    // given
    val messageResponse = ClaudeFixture.sampleMessageResponseJson
    val apiKey = "test-api-key"
    val capturedRequest = new AtomicReference[GenericRequest[_, _]](null)
    val syncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondF { request =>
      capturedRequest.set(request)
      sttp.client4.testing.ResponseStub.adjust(messageResponse, Ok)
    }
    val claudeClient = ClaudeSyncClient(apiKey = apiKey, backend = syncBackendStub)

    // when
    claudeClient.createMessage(ClaudeFixture.sampleMessageRequest)

    // then
    val request = capturedRequest.get()
    request.headers should contain(sttp.model.Header("x-api-key", apiKey))
    request.headers should contain(sttp.model.Header("anthropic-version", "2023-06-01"))
    request.headers should contain(sttp.model.Header("content-type", "application/json"))
  }

  "ClaudeSyncClient" should "use correct base URL" in {
    // given
    val messageResponse = ClaudeFixture.sampleMessageResponseJson
    val capturedRequest = new AtomicReference[GenericRequest[_, _]](null)
    val syncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondF { request =>
      capturedRequest.set(request)
      sttp.client4.testing.ResponseStub.adjust(messageResponse, Ok)
    }
    val claudeClient = ClaudeSyncClient(apiKey = "test-api-key", backend = syncBackendStub)

    // when
    claudeClient.createMessage(ClaudeFixture.sampleMessageRequest)

    // then
    val request = capturedRequest.get()
    request.uri.toString should startWith("https://api.anthropic.com/v1/messages")
  }

  "ClaudeSyncClient" should "handle custom base URL" in {
    // given
    import sttp.model.Uri._
    val customBaseUrl = uri"https://custom-claude-api.com/v1"
    val messageResponse = ClaudeFixture.sampleMessageResponseJson
    val capturedRequest = new AtomicReference[GenericRequest[_, _]](null)
    val syncBackendStub = DefaultSyncBackend.stub.whenAnyRequest.thenRespondF { request =>
      capturedRequest.set(request)
      sttp.client4.testing.ResponseStub.adjust(messageResponse, Ok)
    }
    val claudeClient = ClaudeSyncClient(apiKey = "test-api-key", backend = syncBackendStub, baseUrl = customBaseUrl)

    // when
    claudeClient.createMessage(ClaudeFixture.sampleMessageRequest)

    // then
    val request = capturedRequest.get()
    request.uri.toString should startWith("https://custom-claude-api.com/v1/messages")
  }
}