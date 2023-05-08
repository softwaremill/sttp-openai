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
        model = FineTuneModel.Curie,
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
    val deserializedJsonResponse: Either[Exception, FineTuneResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[FineTuneResponse].apply(jsonResponse)

    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

  "Given create fine tunes request as case class" should "be properly serialized to Json" in {

    val givenRequest = FineTunesRequestBody(
      trainingFile = "file-train231",
      validationFile = Some("file-valid2"),
      model = Some(FineTuneModel.Curie),
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
          model = FineTuneModel.Curie,
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
    val deserializedJsonResponse: Either[Exception, GetFineTunesResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[GetFineTunesResponse].apply(jsonResponse)

    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

  "Given retrieve fine tunes response as Json" should "be properly deserialized to case class" in {

    // given
    val jsonResponse = fixtures.FineTunesFixture.jsonRetrieveFineTuneResponse
    val expectedResponse: FineTuneResponse = FineTuneResponse(
      FineTuneData(
        `object` = "fine-tune",
        id = "ft-qSvHXdwMhuZZqWbXhJFmg21n",
        hyperparams = Hyperparams(
          nEpochs = 4,
          batchSize = Some(1),
          promptLossWeight = 0.01,
          learningRateMultiplier = Some(0.1)
        ),
        organizationId = "org-org123",
        model = FineTuneModel.Curie,
        trainingFiles = Seq(
          FileData(
            `object` = "file",
            id = "file-ntUIeQbt4iFIRNOKsyjDoLFT",
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
            id = "file-6kmg4GvzAumtDSeZ2jlcg3r5",
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
      ),
      events = Seq(
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Created fine-tune: ft-qSvHXdwMhuZZqWbXhJFmg21n",
          createdAt = 1681810958
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune costs $0.00",
          createdAt = 1681811232
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune enqueued. Queue number: 0",
          createdAt = 1681811233
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune started",
          createdAt = 1681811237
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 1/4",
          createdAt = 1681811298
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 2/4",
          createdAt = 1681811298
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 3/4",
          createdAt = 1681811299
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 4/4",
          createdAt = 1681811299
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Uploaded model: curie:ft-personal-2023-04-18-09-48-38",
          createdAt = 1681811319
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Uploaded result file: file-6kmg4GvzAumtDSeZ2jlcg3r5",
          createdAt = 1681811320
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune succeeded",
          createdAt = 1681811320
        )
      )
    )

    // when
    val deserializedJsonResponse: Either[Exception, FineTuneResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[FineTuneResponse].apply(jsonResponse)

    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

  "Given delete fine tune model response as Json" should "be properly deserialized to case class" in {

    // given
    val jsonResponse = fixtures.FineTunesFixture.jsonDeleteFineTuneModel
    val expectedResponse: DeleteFineTuneModelResponse = DeleteFineTuneModelResponse(
      id = "curie:ft-personal-2023-04-18-09-48-38",
      `object` = "model",
      deleted = true
    )
    // when
    val deserializedJsonResponse: Either[Exception, DeleteFineTuneModelResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[DeleteFineTuneModelResponse].apply(jsonResponse)

    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

  "Given list fine tunes events response as Json" should "be properly deserialized to case class" in {

    // given
    val jsonResponse = fixtures.FineTunesFixture.jsonListFineTuneEventsResponse
    val expectedResponse: FineTuneEventsResponse = FineTuneEventsResponse(
      `object` = "list",
      data = Seq(
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Created fine-tune: ft-qSvHXdwMhuZZqWbXhJFmg21n",
          createdAt = 1681810958
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune costs $0.00",
          createdAt = 1681811232
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune enqueued. Queue number: 0",
          createdAt = 1681811233
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune started",
          createdAt = 1681811237
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 1/4",
          createdAt = 1681811298
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 2/4",
          createdAt = 1681811298
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 3/4",
          createdAt = 1681811299
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Completed epoch 4/4",
          createdAt = 1681811299
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Uploaded model: curie:ft-personal-2023-04-18-09-48-38",
          createdAt = 1681811319
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Uploaded result file: file-6kmg4GvzAumtDSeZ2jlcg3r5",
          createdAt = 1681811320
        ),
        Event(
          `object` = "fine-tune-event",
          level = "info",
          message = "Fine-tune succeeded",
          createdAt = 1681811320
        )
      )
    )

    // when
    val deserializedJsonResponse: Either[Exception, FineTuneEventsResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[FineTuneEventsResponse].apply(jsonResponse)

    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

  "Given cancel fine tunes response as Json" should "be properly deserialized to case class" in {

    // given
    val jsonResponse = fixtures.FineTunesFixture.jsonCancelFineTuneResponse
    val expectedResponse: FineTuneResponse = FineTuneResponse(
      FineTuneData(
        id = "ft-xhrpBbvVUzYGo8oUO1FY4nI7",
        `object` = "fine-tune",
        model = FineTuneModel.Curie,
        createdAt = 1614807770,
        fineTunedModel = None,
        hyperparams = Hyperparams(
          nEpochs = 4,
          batchSize = Some(1),
          promptLossWeight = 0.01,
          learningRateMultiplier = Some(0.1)
        ),
        organizationId = "org-org1233",
        trainingFiles = Seq(
          FileData(
            `object` = "file",
            id = "file-train231",
            purpose = "fine-tune-train",
            filename = "my-data-train.jsonl",
            bytes = 1547276,
            createdAt = 1610062281,
            status = "cancelled",
            statusDetails = None
          )
        ),
        validationFiles = Seq.empty[FileData],
        resultFiles = Seq.empty[FileData],
        updatedAt = 1614807789,
        status = "cancelled"
      ),
      Seq.empty[Event]
    )

    // when
    val deserializedJsonResponse: Either[Exception, FineTuneResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[FineTuneResponse].apply(jsonResponse)

    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }
}
