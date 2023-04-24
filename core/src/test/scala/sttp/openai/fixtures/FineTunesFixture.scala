package sttp.openai.fixtures

object FineTunesFixture {
  var jsonCreateFineTuneRequest: String = """{
                                            |  "training_file": "file-train231",
                                            |  "validation_file": "file-valid2",
                                            |  "model": "curie",
                                            |  "n_epochs": 3,
                                            |  "batch_size": 2,
                                            |  "learning_rate_multiplier": 0.04,
                                            |  "prompt_loss_weight": 0.01,
                                            |  "compute_classification_metrics": true,
                                            |  "classification_n_classes": 13,
                                            |  "classification_positive_class": "pClass",
                                            |  "classification_betas": [
                                            |    0.01,
                                            |    0.04,
                                            |    0.2
                                            |  ],
                                            |  "suffix": "sml-model-name"
                                            |}""".stripMargin

  val jsonCreateFineTuneResponse: String = """{
                     |  "object": "fine-tune",
                     |  "id": "ft-trala",
                     |  "hyperparams": {
                     |    "n_epochs": 4,
                     |    "batch_size": null,
                     |    "prompt_loss_weight": 0.01,
                     |    "learning_rate_multiplier": null
                     |  },
                     |  "organization_id": "org-org123",
                     |  "model": "curie",
                     |  "training_files": [
                     |    {
                     |      "object": "file",
                     |      "id": "file-train231",
                     |      "purpose": "fine-tune",
                     |      "filename": "example.jsonl",
                     |      "bytes": 44,
                     |      "created_at": 1681375533,
                     |      "status": "processed",
                     |      "status_details": null
                     |    }
                     |  ],
                     |  "validation_files": [],
                     |  "result_files": [],
                     |  "created_at": 1681810958,
                     |  "updated_at": 1681810958,
                     |  "status": "pending",
                     |  "fine_tuned_model": null,
                     |  "events": [
                     |    {
                     |      "object": "fine-tune-event",
                     |      "level": "info",
                     |      "message": "Created fine-tune: ft-qSvHXdwMhuZZqWbXhJFmg21n",
                     |      "created_at": 1681810958
                     |    }
                     |  ]
                     |}""".stripMargin

  val jsonListFineTuneResponse: String = """{
                                           |  "object": "list",
                                           |  "data": [
                                           |    {
                                           |      "object": "fine-tune",
                                           |      "id": "ft-qSvHXdwMhuZZqWbXhJFmg21n",
                                           |      "hyperparams": {
                                           |        "n_epochs": 4,
                                           |        "batch_size": 1,
                                           |        "prompt_loss_weight": 0.01,
                                           |        "learning_rate_multiplier": 0.1
                                           |      },
                                           |      "organization_id": "org-9Pr8JxSaUX4Czeu1It3IT3hz",
                                           |      "model": "curie",
                                           |      "training_files": [
                                           |        {
                                           |          "object": "file",
                                           |          "id": "file-train231",
                                           |          "purpose": "fine-tune",
                                           |          "filename": "example.jsonl",
                                           |          "bytes": 44,
                                           |          "created_at": 1681375533,
                                           |          "status": "processed",
                                           |          "status_details": null
                                           |        }
                                           |      ],
                                           |      "validation_files": [],
                                           |      "result_files": [
                                           |        {
                                           |          "object": "file",
                                           |          "id": "file-train231",
                                           |          "purpose": "fine-tune-results",
                                           |          "filename": "compiled_results.csv",
                                           |          "bytes": 273,
                                           |          "created_at": 1681811319,
                                           |          "status": "processed",
                                           |          "status_details": null
                                           |        }
                                           |      ],
                                           |      "created_at": 1681810958,
                                           |      "updated_at": 1681811320,
                                           |      "status": "succeeded",
                                           |      "fine_tuned_model": "curie:ft-personal-2023-04-18-09-48-38"
                                           |    }
                                           |  ]
                                           |}
                                           |""".stripMargin

  val jsonRetrieveFineTuneResponse: String = """{
                                       |  "object": "fine-tune",
                                       |  "id": "ft-qSvHXdwMhuZZqWbXhJFmg21n",
                                       |  "hyperparams": {
                                       |    "n_epochs": 4,
                                       |    "batch_size": 1,
                                       |    "prompt_loss_weight": 0.01,
                                       |    "learning_rate_multiplier": 0.1
                                       |  },
                                       |  "organization_id": "org-org123",
                                       |  "model": "curie",
                                       |  "training_files": [
                                       |    {
                                       |      "object": "file",
                                       |      "id": "file-ntUIeQbt4iFIRNOKsyjDoLFT",
                                       |      "purpose": "fine-tune",
                                       |      "filename": "example.jsonl",
                                       |      "bytes": 44,
                                       |      "created_at": 1681375533,
                                       |      "status": "processed",
                                       |      "status_details": null
                                       |    }
                                       |  ],
                                       |  "validation_files": [],
                                       |  "result_files": [
                                       |    {
                                       |      "object": "file",
                                       |      "id": "file-6kmg4GvzAumtDSeZ2jlcg3r5",
                                       |      "purpose": "fine-tune-results",
                                       |      "filename": "compiled_results.csv",
                                       |      "bytes": 273,
                                       |      "created_at": 1681811319,
                                       |      "status": "processed",
                                       |      "status_details": null
                                       |    }
                                       |  ],
                                       |  "created_at": 1681810958,
                                       |  "updated_at": 1681811320,
                                       |  "status": "succeeded",
                                       |  "fine_tuned_model": "curie:ft-personal-2023-04-18-09-48-38",
                                       |  "events": [
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Created fine-tune: ft-qSvHXdwMhuZZqWbXhJFmg21n",
                                       |      "created_at": 1681810958
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Fine-tune costs $0.00",
                                       |      "created_at": 1681811232
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Fine-tune enqueued. Queue number: 0",
                                       |      "created_at": 1681811233
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Fine-tune started",
                                       |      "created_at": 1681811237
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Completed epoch 1/4",
                                       |      "created_at": 1681811298
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Completed epoch 2/4",
                                       |      "created_at": 1681811298
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Completed epoch 3/4",
                                       |      "created_at": 1681811299
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Completed epoch 4/4",
                                       |      "created_at": 1681811299
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Uploaded model: curie:ft-personal-2023-04-18-09-48-38",
                                       |      "created_at": 1681811319
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Uploaded result file: file-6kmg4GvzAumtDSeZ2jlcg3r5",
                                       |      "created_at": 1681811320
                                       |    },
                                       |    {
                                       |      "object": "fine-tune-event",
                                       |      "level": "info",
                                       |      "message": "Fine-tune succeeded",
                                       |      "created_at": 1681811320
                                       |    }
                                       |  ]
                                       |}
                                       |""".stripMargin
}
