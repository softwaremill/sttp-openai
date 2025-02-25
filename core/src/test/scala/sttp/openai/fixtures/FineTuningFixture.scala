package sttp.openai.fixtures

object FineTuningFixture {

  val jsonCreateFineTuneRequest: String = """{
                                            |  "model": "gpt-3.5-turbo-0125",
                                            |  "training_file": "file-train231",
                                            |  "suffix": "sml-model-name",
                                            |  "validation_file": "file-valid2",
                                            |  "integrations": [
                                            |     {
                                            |        "type": "wandb",
                                            |        "wandb": {
                                            |           "project": "sml-project",
                                            |           "name": "sml",
                                            |           "entity": "sml-entity",
                                            |           "tags": [
                                            |              "sml-tag"
                                            |           ]
                                            |        }
                                            |     }
                                            |  ],
                                            |  "seed": 3,
                                            |  "method": {
                                            |     "type": "supervised",
                                            |     "supervised": {
                                            |         "hyperparameters": {
                                            |             "batch_size": 2,
                                            |             "learning_rate_multiplier": 0.2,
                                            |             "n_epochs": 4
                                            |         }
                                            |     },
                                            |     "dpo": {
                                            |         "hyperparameters": {
                                            |             "batch_size": 3,
                                            |             "learning_rate_multiplier": 0.5,
                                            |             "n_epochs": 5,
                                            |             "beta": 0.7
                                            |         }
                                            |     }
                                            |  }
                                            |}""".stripMargin

}
