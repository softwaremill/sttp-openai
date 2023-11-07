package sttp.openai.streaming.fs2

import org.scalatest.EitherValues
import org.scalatest.flatspec.AsyncFlatSpec
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import fs2.{text, Stream}
import sttp.client4.httpclient.fs2.HttpClientFs2Backend
import sttp.client4.testing.RawStream
import sttp.model.sse.ServerSentEvent
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.fixtures.ErrorFixture
import sttp.openai.json.SnakePickle._
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse.DoneEventMessage
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.utils.JsonUtils.compactJson

class Fs2ClientSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with EitherValues {
  for ((statusCode, expectedError) <- ErrorFixture.testData)
    s"Service response with status code: $statusCode" should s"return properly deserialized ${expectedError.getClass.getSimpleName}" in {
      // given
      val fs2BackendStub = HttpClientFs2Backend.stub[IO].whenAnyRequest.thenRespondWithCode(statusCode, ErrorFixture.errorResponse)
      val client = new OpenAI("test-token")

      val givenRequest = ChatBody(
        model = ChatCompletionModel.GPT35Turbo,
        messages = Seq.empty
      )

      // when
      val caught = client
        .createStreamedChatCompletion[IO](givenRequest)
        .send(fs2BackendStub)
        .map(_.body.left.value)

      // then
      caught.asserting { c =>
        c.getClass shouldBe expectedError.getClass
        c.message shouldBe expectedError.message
        c.cause shouldBe expectedError.cause
        c.code shouldBe expectedError.code
        c.param shouldBe expectedError.param
        c.`type` shouldBe expectedError.`type`
      }
    }

  "Creating chat completions with failed stream due to invalid deserialization" should "return properly deserialized error" in {
    // given
    val invalidJson = Some("invalid json")
    val invalidEvent = ServerSentEvent(invalidJson)

    val streamedResponse = Stream
      .emit(invalidEvent.toString)
      .through(text.utf8.encode)
      .covary[IO]

    val fs2BackendStub = HttpClientFs2Backend.stub[IO].whenAnyRequest.thenRespond(RawStream(streamedResponse))
    val client = new OpenAI(authToken = "test-token")

    val givenRequest = ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = Seq.empty
    )

    // when
    val response = client
      .createStreamedChatCompletion[IO](givenRequest)
      .send(fs2BackendStub)
      .map(_.body.value)
      .flatMap(_.compile.drain)

    // then
    response.attempt.asserting(_ shouldBe a[Left[DeserializationOpenAIException, _]])
  }

  "Creating chat completions with successful response" should "return properly deserialized list of chunks" in {
    // given
    val chatChunks = Seq.fill(3)(sttp.openai.fixtures.ChatChunkFixture.jsonResponse).map(compactJson)
    val events = chatChunks.map(data => ServerSentEvent(Some(data))) :+ ServerSentEvent(Some(DoneEventMessage))
    val delimiter = "\n\n"
    val streamedResponse = Stream
      .emits(events)
      .map(_.toString + delimiter)
      .through(text.utf8.encode)
      .covary[IO]

    val fs2BackendStub = HttpClientFs2Backend.stub[IO].whenAnyRequest.thenRespond(RawStream(streamedResponse))
    val client = new OpenAI(authToken = "test-token")

    val givenRequest = ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = Seq.empty
    )

    // when
    val response = client
      .createStreamedChatCompletion[IO](givenRequest)
      .send(fs2BackendStub)
      .map(_.body.value)
      .flatMap(_.compile.toList)

    // then
    val expectedResponse = chatChunks.map(read[ChatChunkResponse](_))
    response.asserting(_ shouldBe expectedResponse)
  }
}
