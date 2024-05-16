package sttp.openai.requests.vectorstore

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures.VectorStoreFixture
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}
import sttp.openai.requests.vectorstore.VectorStoreRequestBody.CreateVectorStoreBody
import sttp.openai.requests.vectorstore.VectorStoreResponseData.{FileCounts, InProgress, VectorStore}

class VectorStoreDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given create vector store request" should "be properly serialized to Json" in {
    // given
    val givenRequest = CreateVectorStoreBody(
      fileIds = Some(Seq("file_1", "file_2")),
      name = Some("vs_1")
    )

    val jsonRequest: ujson.Value = ujson.read(VectorStoreFixture.jsonCreateRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create vector store request with expires" should "be properly serialized to Json" in {
    // given
    val givenRequest = CreateVectorStoreBody(
      fileIds = Some(Seq("file_1", "file_2")),
      name = Some("vs_1"),
      expiresAfter = Some(ExpiresAfter("11111", 2))
    )

    val jsonRequest: ujson.Value = ujson.read(VectorStoreFixture.jsonCreateWithExpiresRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create vector store mode" should "be properly deserialized from Json" in {
    import sttp.openai.requests.vectorstore.VectorStoreResponseData.VectorStore._
    // given
    val givenResponse = VectorStore(
      id = "vs_1",
      `object` = "vector_store",
      createdAt = 1698107661,
      name = "test_vs",
      usageBytes = 123456,
      fileCounts = FileCounts(0, 1, 1, 2, 4),
      status = InProgress,
      expiresAfter = None,
      expiresAt = Some(1698107651),
      lastActiveAt = Some(1698107661),
      lastUsedAt = Some(1698107681)
    )
    val jsonResponse = VectorStoreFixture.jsonObject

    // when
    val serializedJson: Either[Exception, VectorStore] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    serializedJson.value shouldBe givenResponse
  }
}