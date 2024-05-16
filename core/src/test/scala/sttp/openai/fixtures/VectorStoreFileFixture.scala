package sttp.openai.fixtures

object VectorStoreFileFixture {

  val jsonCreateRequest: String =
    """{
      |  "file_id": "file_1"
      |}""".stripMargin

  val jsonObject: String =
    """{
      |  "id": "vsf_1",
      |  "object": "vector_store.file",
      |  "usage_bytes": 123456,
      |  "created_at": 1698107661,
      |  "vector_store_id": "vs_1",
      |  "status": "completed",
      |  "last_error": null
      |}""".stripMargin

  val jsonObjectWithLastError: String =
    """{
      |  "id": "vsf_1",
      |  "object": "vector_store.file",
      |  "usage_bytes": 123456,
      |  "created_at": 1698107661,
      |  "vector_store_id": "vs_1",
      |  "status": "completed",
      |  "last_error": {
      |     "code": "server_error",
      |     "message": "Failed"
      |  }
      |}""".stripMargin
}
