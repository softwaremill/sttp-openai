package sttp.openai.fixtures

object UploadFixture {

  val jsonCreateUpload: String =
    """{
      |  "filename": "file-name",
      |  "purpose": "file-purpose",
      |  "bytes": 123,
      |  "mime_type": "file/mime-type"
      |}""".stripMargin

  val jsonUpdateResponse: String =
    """{
      |  "id": "upload_abc123",
      |  "object": "upload",
      |  "bytes": 1147483648,
      |  "created_at": 1719184911,
      |  "filename": "training_examples.jsonl",
      |  "purpose": "fine-tune",
      |  "status": "completed",
      |  "expires_at": 1719127296,
      |  "file": {
      |    "id": "file-xyz321",
      |    "object": "file",
      |    "bytes": 1147483648,
      |    "created_at": 1719186911,
      |    "filename": "training_examples.jsonl",
      |    "purpose": "fine-tune"
      |  }
      |}""".stripMargin

}
