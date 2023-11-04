package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.SttpUpickleApiExtension

class ChatChunkDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given chat chunk completions response as Json" should "be properly deserialized to case class" in {
    import ChatChunkRequestResponseData._
    import ChatChunkRequestResponseData.ChatChunkResponse._

    // given
    val jsonResponse = fixtures.ChatChunkFixture.jsonResponse

    val functionCall: FunctionCall = FunctionCall(
      arguments = "args",
      name = "Fish"
    )

    val delta: Delta = Delta(
      role = Some(Role.Assistant),
      content = Some("  Hi"),
      functionCall = Some(functionCall)
    )

    val choices: Choices = Choices(
      delta = delta,
      finishReason = "stop",
      index = 0
    )

    val expectedResponse: ChatChunkResponse = ChatChunkResponse(
      id = "chatcmpl-76FxnKOjnPkDVYTAQ1wK8iUNFJPvR",
      `object` = "chat.completion",
      created = 1681725687,
      model = "gpt-3.5-turbo-0301",
      choices = Seq(choices)
    )

    // when
    val givenResponse: Either[Exception, ChatChunkResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

}
