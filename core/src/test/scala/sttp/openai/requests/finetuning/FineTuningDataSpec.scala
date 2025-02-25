package sttp.openai.requests.finetuning

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}
import sttp.openai.requests.finetuning.FineTuningModel.GPT35Turbo0125
import sttp.openai.requests.finetuning.Status.Running

class FineTuningDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given create fine tuning request as case class" should "be properly serialized to Json" in {
    // given
    val givenRequest = FineTuningRequestBody(
      model = GPT35Turbo0125,
      trainingFile = "file-train231",
      suffix = Some("sml-model-name"),
      validationFile = Some("file-valid2"),
      integrations = Some(
        Seq(
          Integration(
            `type` = Integration.Wandb,
            wandb = Wandb(
              project = "sml-project",
              name = Some("sml"),
              entity = Some("sml-entity"),
              tags = Some(Seq("sml-tag"))
            )
          )
        )
      ),
      seed = Some(3),
      method = Some(
        Method(
          `type` = Some(Method.Supervised),
          supervised = Some(
            Supervised(
              hyperparameters = Some(
                Hyperparameters(
                  batchSize = Some(2),
                  learningRateMultiplier = Some(0.2f),
                  nEpochs = Some(4)
                )
              )
            )
          ),
          dpo = Some(
            Dpo(
              hyperparameters = Some(
                Hyperparameters(
                  batchSize = Some(3),
                  learningRateMultiplier = Some(0.5f),
                  nEpochs = Some(5),
                  beta = Some(0.7f)
                )
              )
            )
          )
        )
      )
    )
    val jsonRequest: ujson.Value = ujson.read(fixtures.FineTuningFixture.jsonCreateFineTuneRequest)
    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)
    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create fine tuning response as Json" should "be properly deserialized to case class" in {
    // given
    val jsonResponse = fixtures.FineTuningFixture.jsonCreateFineTuneResponse
    val expectedResponse: FineTuningResponse = FineTuningResponse(
      id = "ft-id",
      createdAt = 1000,
      error = Some(
        Error(
          code = "error-code",
          message = "error-message",
          param = Some("error-param")
        )
      ),
      fineTunedModel = Some("ft-model"),
      finishedAt = Some(2000),
      hyperparameters = Some(
        Hyperparameters(
          batchSize = Some(2),
          learningRateMultiplier = Some(0.2f),
          nEpochs = Some(4)
        )
      ),
      model = "ft-model",
      `object` = "ft-object",
      organizationId = "ft-org-id",
      resultFiles = Seq("ft-rf"),
      status = Running,
      trainedTokens = Some(3000),
      trainingFile = "ft-tf",
      validationFile = Some("ft-vf"),
      integrations = Some(
        Seq(
          Integration(
            `type` = Integration.Wandb,
            wandb = Wandb(
              project = "sml-project",
              name = Some("sml"),
              entity = Some("sml-entity"),
              tags = Some(Seq("sml-tag"))
            )
          )
        )
      ),
      seed = 1,
      estimatedFinish = Some(4000),
      method = Method(
        `type` = Some(Method.Supervised),
        supervised = Some(
          Supervised(
            hyperparameters = Some(
              Hyperparameters(
                batchSize = Some(2),
                learningRateMultiplier = Some(0.2f),
                nEpochs = Some(4)
              )
            )
          )
        ),
        dpo = Some(
          Dpo(
            hyperparameters = Some(
              Hyperparameters(
                batchSize = Some(3),
                learningRateMultiplier = Some(0.5f),
                nEpochs = Some(5),
                beta = Some(0.7f)
              )
            )
          )
        )
      )
    )
    // when
    val deserializedJsonResponse: Either[Exception, FineTuningResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[FineTuningResponse].apply(jsonResponse)
    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

}
