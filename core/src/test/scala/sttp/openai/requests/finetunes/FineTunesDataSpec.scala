package sttp.openai.requests.finetunes

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}
import sttp.openai.fixtures
import sttp.openai.requests.files.FilesResponseData.FileData
import sttp.openai.requests.finetunes.FineTunesResponseData._

class FineTunesDataSpec extends AnyFlatSpec with Matchers with EitherValues {
  "Given create fine tunes response as Json" should "be properly deserialized to case class" in {

    // given
    val jsonResponse = fixtures.FineTunesFixture.jsonCreateFineTuneResponse
    val expectedResponse: FineTuneResponse = FineTuneResponse(
      FineTuneData(
        `object` = "fine-tune",
        id = "ft-trala",
        hyperparams = Hyperparams(
          nEpochs = 4,
          batchSize = None,
          promptLossWeight = 0.01,
          learningRateMultiplier = None
        ),
        organizationId = "org-org123",
        model = "curie",
        trainingFiles = Seq(
          FileData(
            `object` = "file",
            id = "file-train231",
            purpose = "fine-tune",
            filename = "example.jsonl",
            bytes = 44,
            createdAt = 1681375533,
            status = "processed",
            statusDetails = None
          )
        ),
        validationFiles = Seq.empty[FileData],
        resultFiles = Seq.empty[FileData],
        createdAt = 1681810958,
        updatedAt = 1681810958,
        status = "pending",
        fineTunedModel = None
      ),
      events = Seq(
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Created fine-tune: ft-qSvHXdwMhuZZqWbXhJFmg21n",
          createdAt = 1681810958
        )
      )
    )

    // when
    val givenResponse: Either[Exception, FineTuneResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[FineTuneResponse].apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

  "Given create fine tunes request as case class" should "be properly serialized to Json" in {

    val givenRequest = FineTunesRequestBody(
      trainingFile = "file-train231",
      validationFile = Some("file-valid2"),
      model = Some("curie"),
      nEpochs = Some(3),
      batchSize = Some(2),
      learningRateMultiplier = Some(0.04d),
      promptLossWeight = Some(0.01d),
      computeClassificationMetrics = Some(true),
      classificationNClasses = Some(13),
      classificationPositiveClass = Some("pClass"),
      classificationBetas = Some(Seq(0.01d, 0.04d, 0.2d)),
      suffix = Some("sml-model-name")
    )
    val jsonRequest: ujson.Value = ujson.read(fixtures.FineTunesFixture.jsonCreateFineTuneRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given list fine tunes response as Json" should "be properly deserialized to case class" in {

    // given
    val jsonResponse = fixtures.FineTunesFixture.jsonListFineTuneResponse
    val expectedResponse: GetFineTunesResponse = GetFineTunesResponse(
      `object` = "list",
      Seq(
        FineTuneData(
          `object` = "fine-tune",
          id = "ft-qSvHXdwMhuZZqWbXhJFmg21n",
          hyperparams = Hyperparams(
            nEpochs = 4,
            batchSize = Some(1),
            promptLossWeight = 0.01,
            learningRateMultiplier = Some(0.1)
          ),
          organizationId = "org-9Pr8JxSaUX4Czeu1It3IT3hz",
          model = "curie",
          trainingFiles = Seq(
            FileData(
              `object` = "file",
              id = "file-train231",
              purpose = "fine-tune",
              filename = "example.jsonl",
              bytes = 44,
              createdAt = 1681375533,
              status = "processed",
              statusDetails = None
            )
          ),
          validationFiles = Seq.empty[FileData],
          resultFiles = Seq(
            FileData(
              `object` = "file",
              id = "file-train231",
              purpose = "fine-tune-results",
              filename = "compiled_results.csv",
              bytes = 273,
              createdAt = 1681811319,
              status = "processed",
              statusDetails = None
            )
          ),
          createdAt = 1681810958,
          updatedAt = 1681811320,
          status = "succeeded",
          fineTunedModel = Some("curie:ft-personal-2023-04-18-09-48-38")
        )
      )
    )

    // when
    val givenResponse: Either[Exception, GetFineTunesResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[GetFineTunesResponse].apply(jsonResponse)

    // then
    givenResponse.value shouldBe expectedResponse
  }

}
