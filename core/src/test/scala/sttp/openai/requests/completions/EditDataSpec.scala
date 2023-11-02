package sttp.openai.requests.completions

import sttp.openai.fixtures
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.json.{SnakePickle, SttpOpenAIApi}
import sttp.openai.requests.completions.edit.EditRequestBody._
import sttp.openai.requests.completions.edit.EditRequestBody.EditModel.TextDavinciEdit001

class EditDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given chat completions response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.completions.edit.EditRequestResponseData._
    import sttp.openai.requests.completions.edit.EditRequestResponseData.EditResponse._

    // given
    val jsonResponse = fixtures.EditFixture.jsonResponse

    val choices: Choices = Choices(
      text = "What day of the week is it?",
      index = 0
    )

    val usage: Usage = Usage(
      promptTokens = 25,
      completionTokens = 32,
      totalTokens = 57
    )

    val expectedResponse: EditResponse = EditResponse(
      `object` = "edit",
      created = 1681798630,
      choices = Seq(choices),
      usage = usage
    )

    // when
    val givenResponse: Either[Exception, EditResponse] = SttpOpenAIApi.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given completions request as case class" should "be properly serialized to Json" in {
    // given
    val givenRequest: EditBody = EditBody(
      model = TextDavinciEdit001,
      input = Some("What day of the wek is it?"),
      instruction = "Fix the spelling mistakes"
    )

    val jsonRequest: ujson.Value = ujson.read(fixtures.EditFixture.jsonRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

}
