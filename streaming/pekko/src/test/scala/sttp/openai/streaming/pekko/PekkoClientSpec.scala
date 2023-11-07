package sttp.openai.streaming.pekko

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl._
import org.apache.pekko.util.ByteString
import org.scalatest.EitherValues
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client4.pekkohttp.PekkoHttpBackend
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

class PekkoClientSpec extends AsyncFlatSpec with Matchers with EitherValues {
  implicit val system: ActorSystem = ActorSystem()

  for ((statusCode, expectedError) <- ErrorFixture.testData)
    s"Service response with status code: $statusCode" should s"return properly deserialized ${expectedError.getClass.getSimpleName}" in {
      // given
      val pekkoBackendStub = PekkoHttpBackend.stub.whenAnyRequest.thenRespondWithCode(statusCode, ErrorFixture.errorResponse)
      val client = new OpenAI("test-token")

      val givenRequest = ChatBody(
        model = ChatCompletionModel.GPT35Turbo,
        messages = Seq.empty
      )

      // when
      val caught = client
        .createStreamedChatCompletion(givenRequest)
        .send(pekkoBackendStub)
        .map(_.body.left.value)

      // then
      caught.map { c =>
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

    val streamedResponse = Source
      .single(invalidEvent.toString)
      .map(ByteString(_))

    val pekkoBackendStub = PekkoHttpBackend.stub.whenAnyRequest.thenRespond(RawStream(streamedResponse))
    val client = new OpenAI(authToken = "test-token")

    val givenRequest = ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = Seq.empty
    )

    // when
    val response = client
      .createStreamedChatCompletion(givenRequest)
      .send(pekkoBackendStub)
      .map(_.body.value)
      .flatMap(_.run())

    // then
    response.failed.map(_ shouldBe a[DeserializationOpenAIException])
  }

  "Creating chat completions with successful response" should "return properly deserialized list of chunks" in {
    // given
    val chatChunks = Seq.fill(3)(sttp.openai.fixtures.ChatChunkFixture.jsonResponse).map(compactJson)
    val events = chatChunks.map(data => ServerSentEvent(Some(data))) :+ ServerSentEvent(Some(DoneEventMessage))
    val delimiter = "\n\n"
    val streamedResponse = Source(events)
      .map(_.toString + delimiter)
      .map(ByteString(_))

    val pekkoBackendStub = PekkoHttpBackend.stub.whenAnyRequest.thenRespond(RawStream(streamedResponse))
    val client = new OpenAI(authToken = "test-token")

    val givenRequest = ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = Seq.empty
    )

    // when
    val response = client
      .createStreamedChatCompletion(givenRequest)
      .send(pekkoBackendStub)
      .map(_.body.value)
      .flatMap(_.runWith(Sink.seq))

    // then
    val expectedResponse = chatChunks.map(read[ChatChunkResponse](_))
    response.map(_ shouldBe expectedResponse)
  }
}
