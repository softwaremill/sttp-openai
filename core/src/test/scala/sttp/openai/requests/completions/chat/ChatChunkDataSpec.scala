package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.SttpUpickleApiExtension
import sttp.openai.requests.completions.Stop.SingleStop
import sttp.openai.utils.ChatCompletionFixtures._

class ChatChunkDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given chat chunk completions response as Json" should "be properly deserialized to case class" in {
    import ChatChunkRequestResponseData.ChatChunkResponse._
    import ChatChunkRequestResponseData._

    // given
    val jsonResponse = fixtures.ChatChunkFixture.jsonResponse

    val delta: Delta = Delta(
      role = Some(Role.Assistant),
      content = Some("  Hi"),
      toolCalls = toolCalls
    )

    val choices: Choices = Choices(
      delta = delta,
      finishReason = Some("stop"),
      index = 0
    )

    val expectedResponse: ChatChunkResponse = ChatChunkResponse(
      id = "chatcmpl-76FxnKOjnPkDVYTAQ1wK8iUNFJPvR",
      `object` = "chat.completion",
      created = 1681725687,
      model = "gpt-3.5-turbo-0301",
      choices = Seq(choices),
      systemFingerprint = Some("systemFingerprint")
    )

    // when
    val givenResponse: Either[Exception, ChatChunkResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given completions request with streaming enabled as case class" should "be properly serialized to Json" in {
    import ChatRequestBody._
    import sttp.openai.requests.completions.chat.message._

    // given
    val givenRequest = ChatRequestBody.ChatBody(
      messages = messages,
      model = ChatCompletionModel.GPT35Turbo,
      frequencyPenalty = Some(0),
      maxTokens = Some(7),
      n = Some(1),
      presencePenalty = Some(0),
      temperature = Some(1),
      topP = Some(1),
      tools = Some(tools),
      responseFormat = Some(ResponseFormat.Text),
      toolChoice = Some(ToolChoice.ToolAuto),
      stop = Some(SingleStop("\n")),
      user = Some("testUser")
    )

    val jsonRequest = ujson.read(fixtures.ChatChunkFixture.jsonRequest)

    // when
    val serializedJson = ChatBody.withStreaming(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }
}
