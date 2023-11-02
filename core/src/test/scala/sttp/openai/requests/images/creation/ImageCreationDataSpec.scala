package sttp.openai.requests.images.creation

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.{SnakePickle, SttpOpenAIApi}
import sttp.openai.requests.images.{ResponseFormat, Size}
class ImageCreationDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given image generation response as Json" should "be properly deserialized to case class" in {
    import sttp.openai.requests.images.ImageResponseData._
    import sttp.openai.requests.images.ImageResponseData.ImageResponse._

    // given
    val jsonResponse = fixtures.ImageCreationFixture.jsonResponse

    val generatedImageData = Seq(
      GeneratedImageData("https://generated.image.url")
    )

    val expectedResponse: ImageResponse = ImageResponse(
      created = 1681893694,
      data = generatedImageData
    )
    // when
    val givenResponse = SttpOpenAIApi.deserializeJsonSnake.apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given create image request as case class" should "be properly serialized to Json" in {
    import sttp.openai.requests.images.creation.ImageCreationRequestBody._
    import sttp.openai.requests.images.creation.ImageCreationRequestBody.ImageCreationBody._

    // given
    val givenRequest: ImageCreationBody = ImageCreationBody(
      prompt = "cute fish",
      Some(1),
      size = Some(Size.Custom("1024x1024")),
      Some(ResponseFormat.Custom("url")),
      Some("user1")
    )

    val jsonRequest = ujson.read(fixtures.ImageCreationFixture.jsonRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create image request as case class created with enum values" should "be properly serialized to Json" in {
    import sttp.openai.requests.images.creation.ImageCreationRequestBody.ImageCreationBody._
    import sttp.openai.requests.images.creation.ImageCreationRequestBody._

    // given
    val givenRequest: ImageCreationBody = ImageCreationBody(
      "cute fish",
      Some(1),
      Some(Size.Large),
      Some(ResponseFormat.URL),
      Some("user1")
    )

    val jsonRequest = ujson.read(fixtures.ImageCreationFixture.jsonRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

}
