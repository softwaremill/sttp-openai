package sttp.openai.models.requests

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import resources.ModelsGetResponse
import sttp.openai.models.requests.ModelsGetResponseData.{Data, ModelsResponse, Permission}
import sttp.openai.models.requests.ModelsGetResponseData.ModelsResponse._

class ModelsGetResponseDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given models response as Json" should "be properly deserialized to case class" in {

    // given
    val response: String = ModelsGetResponse.responseJson

    val babbagePermission: Seq[Permission] = Seq(
      Permission(
        id = "modelperm-49FUp5v084tBB49tC4z8LPH5",
        `object` = "model_permission",
        created = 1669085501,
        allowCreateEngine = false,
        allowSampling = true,
        allowLogprobs = true,
        allowSearchIndices = false,
        allowView = true,
        allowFineTuning = false,
        organization = "*",
        group = None,
        isBlocking = false
      )
    )

    val davinciPermission: Seq[Permission] =
      Seq(
        Permission(
          id = "modelperm-U6ZwlyAd0LyMk4rcMdz33Yc3",
          `object` = "model_permission",
          created = 1669066355,
          allowCreateEngine = false,
          allowSampling = true,
          allowLogprobs = true,
          allowSearchIndices = false,
          allowView = true,
          allowFineTuning = false,
          organization = "*",
          group = None,
          isBlocking = false
        )
      )

    val serializedData: Seq[Data] = Seq(
      Data(
        id = "babbage",
        `object` = "model",
        created = 1649358449,
        ownedBy = "openai",
        permission = babbagePermission,
        root = "babbage",
        parent = None
      ),
      Data(
        id = "davinci",
        `object` = "model",
        created = 1649359874,
        ownedBy = "openai",
        permission = davinciPermission,
        root = "davinci",
        parent = None
      )
    )

    val expectedResponse: ModelsResponse = ModelsResponse(`object` = "list", data = serializedData)
    // when

    val givenResponse: Either[Exception, ModelsResponse] = SttpUpickleApiExtension.deserializeJsonSnake.apply(response)

    // then
    givenResponse.value shouldBe expectedResponse
  }
}
