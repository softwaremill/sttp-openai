package sttp.openai.requests.vectorstore.file

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures.VectorStoreFileFixture
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}
import sttp.openai.requests.vectorstore.file.VectorStoreFileResponseData.{LastError, ServerError, VectorStoreFile}
import sttp.openai.requests.vectorstore.file.VectorStoreFileRequestBody.CreateVectorStoreFileBody

class VectorStoreFileDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given create vector store file request" should "be properly serialized to Json" in {
    // given
    val givenRequest = CreateVectorStoreFileBody(
      fileId = "file_1"
    )

    val jsonRequest: ujson.Value = ujson.read(VectorStoreFileFixture.jsonCreateRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Vector store file response" should "be properly deserialized from Json" in {
    import sttp.openai.requests.vectorstore.file.VectorStoreFileResponseData.VectorStoreFile._
    // given
    val givenResponse = VectorStoreFile(
      id = "vsf_1",
      `object` = "vector_store.file",
      createdAt = 1698107661,
      usageBytes = 123456,
      status = Completed,
      vectorStoreId = "vs_1",
    )
    val jsonResponse = VectorStoreFileFixture.jsonObject

    // when
    val serializedJson: Either[Exception, VectorStoreFile] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    serializedJson.value shouldBe givenResponse
  }

  "Vector store file response with error" should "be properly deserialized from Json" in {
    import sttp.openai.requests.vectorstore.file.VectorStoreFileResponseData.VectorStoreFile._
    // given
    val givenResponse = VectorStoreFile(
      id = "vsf_1",
      `object` = "vector_store.file",
      createdAt = 1698107661,
      usageBytes = 123456,
      status = Completed,
      vectorStoreId = "vs_1",
      lastError = Some(LastError(ServerError, "Failed"))
    )
    val jsonResponse = VectorStoreFileFixture.jsonObjectWithLastError

    // when
    val serializedJson: Either[Exception, VectorStoreFile] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    serializedJson.value shouldBe givenResponse
  }
}