package sttp.openai.fixtures

object FilesResponse {
  val listFilesJsonResponse: String = """{
                                |  "object": "list",
                                |  "data": [
                                |    {
                                |      "object": "file",
                                |      "id": "file-tralala",
                                |      "purpose": "fine-tune",
                                |      "filename": "example.jsonl",
                                |      "bytes": 44,
                                |      "created_at": 1681375533,
                                |      "status": "processed",
                                |      "status_details": null
                                |    }
                                |  ]
                                |}""".stripMargin

  val retrieveFileJsonResponse: String = """{
                                           |  "object": "file",
                                           |  "id": "file-tralala",
                                           |  "purpose": "fine-tune",
                                           |  "filename": "example.jsonl",
                                           |  "bytes": 44,
                                           |  "created_at": 1681375533,
                                           |  "status": "processed",
                                           |  "status_details": null
                                           |}""".stripMargin
}
