package sttp.openai.requests

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures.BatchFixture
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}
import sttp.openai.requests.batch.{BatchRequestBody, BatchResponse, RequestCounts}

class BatchDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given create batch request as case class" should "be properly serialized to Json" in {
    // given
    val givenRequest = BatchRequestBody(
      inputFileId = "file-id",
      endpoint = "/v1/chat/completions",
      completionWindow = "24h",
      metadata = Some(Map("key1" -> "value1", "key2" -> "value2"))
    )
    val jsonRequest: ujson.Value = ujson.read(BatchFixture.jsonCreateBatchRequest)
    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)
    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create batch response as Json" should "be properly deserialized to case class" in {
    // given
    val jsonResponse = BatchFixture.jsonCreateBatchResponse
    val expectedResponse: BatchResponse = BatchResponse(
      id = "batch_abc123",
      endpoint = "/v1/completions",
      errors = None,
      inputFileId = "file-abc123",
      completionWindow = "24h",
      status = "completed",
      outputFileId = Some("file-cvaTdG"),
      errorFileId = Some("file-HOWS94"),
      createdAt = 1711471533,
      inProgressAt = Some(1711471538),
      expiresAt = Some(1711557933),
      finalizingAt = Some(1711493133),
      completedAt = Some(1711493163),
      failedAt = None,
      expiredAt = None,
      cancellingAt = None,
      cancelledAt = None,
      requestCounts = Some(RequestCounts(total = 100, completed = 95, failed = 5)),
      metadata = Some(Map("customer_id" -> "user_123456789", "batch_description" -> "Nightly eval job"))
    )
    // when
    val deserializedJsonResponse: Either[Exception, BatchResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[BatchResponse].apply(jsonResponse)
    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

}
