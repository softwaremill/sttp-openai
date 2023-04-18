package sttp.openai.fixtures

object FineTunesFixture {
  val jsonCreateFineTuneResponse = """{
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
}
