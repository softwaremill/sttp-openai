package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.SnakePickle
import sttp.openai.json.SttpUpickleApiExtension
import sttp.openai.requests.completions.Stop.SingleStop
import sttp.openai.requests.completions.Usage

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

    val functionCall: FunctionCall = FunctionCall(
      arguments = "args",
      name = "Fish"
    )

    val message: Message = Message(
      role = Role.Assistant,
      content = "Hi there! How can I assist you today?",
      functionCall = Some(functionCall)
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
      choices = Seq(choices)
    )

    // when
    val givenResponse: Either[Exception, ChatResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given completions request as case class" should "be properly serialized to Json" in {
    import ChatRequestBody._

    // given
    val functionCall: FunctionCall = FunctionCall(
      arguments = "args",
      name = "Fish"
    )

    val messages: Seq[Message] = Seq(
      Message(
        role = Role.User,
        content = "Hello!",
        name = Some("Andrzej"),
        functionCall = Some(functionCall)
      )
    )

    val givenRequest = ChatRequestBody.ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = messages,
      temperature = Some(1),
      topP = Some(1),
      n = Some(1),
      stop = Some(SingleStop("\n")),
      maxTokens = Some(7),
      presencePenalty = Some(0),
      frequencyPenalty = Some(0),
      user = Some("testUser")
    )

    val jsonRequest: ujson.Value = ujson.read(fixtures.ChatFixture.jsonRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest

  }

}
