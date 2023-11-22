package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.SnakePickle
import sttp.openai.json.SttpUpickleApiExtension
import sttp.openai.requests.completions.Stop.SingleStop
import sttp.openai.requests.completions.Usage
import sttp.openai.utils.ChatCompletionFixtures._

class ChatDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given chat completions response as Json" should "be properly deserialized to case class" in {
    import ChatRequestResponseData._
    import ChatRequestResponseData.ChatResponse._

    // given
    val jsonResponse = fixtures.ChatFixture.jsonResponse

    val usage: Usage = Usage(
      promptTokens = 10,
      completionTokens = 10,
      totalTokens = 20
    )

    val message: Message = Message(
      role = Role.Assistant,
      content = "Hi there! How can I assist you today?",
      toolCalls = toolCalls
    )

    val choices: Choices = Choices(
      message = message,
      finishReason = "stop",
      index = 0
    )

    val expectedResponse: ChatResponse = ChatResponse(
      id = "chatcmpl-76FxnKOjnPkDVYTAQ1wK8iUNFJPvR",
      `object` = "chat.completion",
      created = 1681725687,
      model = "gpt-3.5-turbo-0301",
      usage = usage,
      choices = Seq(choices),
      systemFingerprint = Some("systemFingerprint")
    )

    // when
    val givenResponse: Either[Exception, ChatResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given completions request as case class" should "be properly serialized to Json" in {
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
      responseFormat = Some(ResponseFormat.JsonObject),
      toolChoice = Some(ToolChoice.ToolFunction("function")),
      stop = Some(SingleStop("\n")),
      user = Some("testUser")
    )

    val jsonRequest: ujson.Value = ujson.read(fixtures.ChatFixture.jsonRequest)

    // when
    val serializedJson = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }
}
