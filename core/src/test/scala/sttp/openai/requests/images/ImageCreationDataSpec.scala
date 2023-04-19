package sttp.openai.requests.images

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}
class ImageCreationDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given image generation response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.images.ImageCreationResponseData._
    import sttp.openai.requests.images.ImageCreationResponseData.ImageCreationResponse._

    // given
    val jsonResponse = fixtures.ImageCreationFixture.jsonResponse

    val generatedImageData = Seq(
      GeneratedImageData("https://generated.image.url")
    )

    val expectedResponse: ImageCreationResponse = ImageCreationResponse(
      created = 1681893694,
      data = generatedImageData
    )
    // when
    val givenResponse = SttpUpickleApiExtension.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given create image request as case class" should "be properly serialized to Json" in {
    import sttp.openai.requests.images.ImageCreationRequestBody._
    import sttp.openai.requests.images.ImageCreationRequestBody.ImageCreationBody._

    // given
    val givenRequest: ImageCreationBody = ImageCreationBody(
      prompt = "cute fish",
      Some(1),
      size = Some("1024x1024"),
      Some("test"),
      Some("user1")
    )

    val jsonRequest = ujson.read(fixtures.ImageCreationFixture.jsonRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

}
