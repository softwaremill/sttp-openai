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

  val jsonCreateFineTuneResponse: String = """{
                                             |  "id": "ft-id",
                                             |  "created_at": 1000,
                                             |  "error": {
                                             |      "code": "error-code",
                                             |      "message": "error-message",
                                             |      "param": "error-param"
                                             |  },
                                             |  "fine_tuned_model": "ft-model",
                                             |  "finished_at": 2000,
                                             |  "hyperparameters": {
                                             |      "batch_size": 2,
                                             |      "learning_rate_multiplier": 0.2,
                                             |      "n_epochs": 4
                                             |  },
                                             |  "model": "ft-model",
                                             |  "object": "ft-object",
                                             |  "organization_id": "ft-org-id",
                                             |  "result_files": [
                                             |      "ft-rf"
                                             |  ],
                                             |  "status": "running",
                                             |  "trained_tokens": 3000,
                                             |  "training_file": "ft-tf",
                                             |  "validation_file": "ft-vf",
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
                                             |  "seed": 1,
                                             |  "estimated_finish": 4000,
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
