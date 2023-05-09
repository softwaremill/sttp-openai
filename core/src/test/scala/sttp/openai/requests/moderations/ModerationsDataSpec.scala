package sttp.openai.requests.moderations

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.SttpUpickleApiExtension
import sttp.openai.requests.moderations.ModerationsRequestBody.ModerationModel
import sttp.openai.requests.moderations.ModerationsResponseData._

class ModerationsDataSpec extends AnyFlatSpec with Matchers with EitherValues {
  "Given create moderation response as Json" should "be properly deserialized to case class" in {
    // given
    val listFilesResponse = fixtures.ModerationsFixture.jsonCreateModerationResponse
    val expectedResponse = ModerationData(
      id = "modr-5MWoLO",
      model = ModerationModel.TextModerationStable,
      results = Seq(
        Result(
          flagged = true,
          categories = Categories(
            sexual = false,
            hate = false,
            violence = true,
            `self-harm` = false,
            `sexual/minors` = false,
            `hate/threatening` = true,
            `violence/graphic` = false
          ),
          categoryScores = CategoryScores(
            sexual = 0.01407341007143259,
            hate = 0.22714105248451233,
            violence = 0.9223177433013916,
            `self-harm` = 0.005232391878962517,
            `sexual/minors` = 0.0038522258400917053,
            `hate/threatening` = 0.4132447838783264,
            `violence/graphic` = 0.036865197122097015
          )
        )
      )
    )
    // when
    val givenResponse: Either[Exception, ModerationData] =
      SttpUpickleApiExtension.deserializeJsonSnake[ModerationData].apply(listFilesResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

}
