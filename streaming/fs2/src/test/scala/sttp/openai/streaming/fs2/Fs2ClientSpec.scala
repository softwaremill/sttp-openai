package sttp.openai.streaming.fs2

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import fs2.{text, Stream}
import sttp.client4.DeserializationException
import sttp.client4.httpclient.fs2.HttpClientFs2Backend
import sttp.client4.testing.RawStream
import sttp.model.sse.ServerSentEvent
import sttp.openai.OpenAI
import sttp.openai.fixtures.ErrorFixture
import sttp.openai.json.SnakePickle._
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse.DoneEventMessage
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.streaming.fs2.Fs2OpenAI._

class Fs2ClientSpec extends AnyFlatSpec with Matchers with EitherValues {
  import cats.effect.unsafe.implicits.global

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
        .unsafeRunSync()

      // then
      caught.getClass shouldBe expectedError.getClass
      caught.message shouldBe expectedError.message
      caught.cause shouldBe expectedError.cause
      caught.code shouldBe expectedError.code
      caught.param shouldBe expectedError.param
      caught.`type` shouldBe expectedError.`type`
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
    response.attempt.unsafeRunSync() shouldBe a[Left[DeserializationException[_], _]]
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
    response.unsafeRunSync() shouldBe expectedResponse
  }

  private def compactJson(json: String): String = write(read[ujson.Value](json))
}
