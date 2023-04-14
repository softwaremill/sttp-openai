package sttp.openai.requests.completions

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}

class CompletionsDataSpec extends AnyFlatSpec with Matchers with EitherValues {
  "Given completions response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.completions.CompletionsResponseData._
    import sttp.openai.requests.completions.CompletionsResponseData.CompletionsResponse._

    // given
    val jsonResponse = fixtures.CompletionsFixture.jsonResponse
    val expectedResponse: CompletionsResponse = CompletionsResponse(
      id = "cmpl-75C628xoevz3eE8zsTFDumZ5wqwmY",
      `object` = "text_completion",
      created = 1681472494,
      model = "text-davinci-003",
      choices = Seq(
        Choices(
          text = "\n\nThis is indeed a test.",
          index = 0,
          logprobs = None,
          finishReason = "stop"
        )
      ),
      usage = Usage(
        promptTokens = 5,
        completionTokens = 8,
        totalTokens = 13
      )
    )

    // when
    val givenResponse: Either[Exception, CompletionsResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given completions request as case class" should "be properly serialized to Json" in {
    import sttp.openai.requests.completions.CompletionsRequestBody._
    import sttp.openai.requests.completions.CompletionsRequestBody.CompletionsBody._

    // given
    val givenRequest = CompletionsRequestBody.CompletionsBody(
      model = "text-davinci-003",
      prompt = Some(SinglePrompt("Say this is a test")),
      maxTokens = Some(7),
      temperature = Some(0),
      topP = Some(1),
      n = Some(1),
      stream = Some(false),
      logprobs = None,
      stop = Some(SingleStop("\n"))
    )
    val jsonRequest = fixtures.CompletionsFixture.jsonRequest

    // when
    val serializedJson = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson.toString().filterNot(_.isWhitespace) shouldBe jsonRequest.filterNot(_.isWhitespace)
  }
}
