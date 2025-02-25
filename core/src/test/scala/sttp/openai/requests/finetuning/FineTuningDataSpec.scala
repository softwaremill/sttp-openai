package sttp.openai.requests.finetuning

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures
import sttp.openai.json.SnakePickle
import sttp.openai.requests.finetuning.FineTuningModel.GPT35Turbo0125

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

}
