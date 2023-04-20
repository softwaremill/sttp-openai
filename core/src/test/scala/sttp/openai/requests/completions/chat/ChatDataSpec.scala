package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}
import sttp.openai.requests.completions.Usage
import sttp.openai.requests.completions.Stop.SingleStop

class ChatDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given chat completions response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.completions.chat.ChatRequestResponseData._
    import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse._

    // given
    val jsonResponse = fixtures.ChatFixture.jsonResponse

    val usage: Usage = Usage(
      promptTokens = 10,
      completionTokens = 10,
      totalTokens = 20
    )

    val message: Message = Message(
      role = "assistance",
      content = "Hi there! How can I assist you today?"
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
    // given
    val messages: Seq[Message] = Seq(
      Message(
        role = "user",
        content = "Hello!"
      )
    )

    val givenRequest = ChatRequestBody.ChatBody(
      model = "gpt-3.5-turbo",
      messages = messages,
      temperature = Some(1),
      topP = Some(1),
      n = Some(1),
      stream = Some(false),
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
