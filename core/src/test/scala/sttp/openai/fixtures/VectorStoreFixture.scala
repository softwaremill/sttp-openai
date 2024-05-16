package sttp.openai.fixtures

object VectorStoreFixture {

  val jsonCreateRequest: String =
    """{
      |  "file_ids": ["file_1", "file_2"],
      |  "name": "vs_1"
      |}""".stripMargin

  val jsonCreateWithExpiresRequest: String =
    """{
      |  "file_ids": ["file_1", "file_2"],
      |  "name": "vs_1",
      |  "expires_after": {
      |   "anchor": "11111",
      |   "days": 2
      |   }
      |}""".stripMargin

  val jsonObject: String =
    """{
      |  "id": "vs_1",
      |  "object": "vector_store",
      |  "created_at": 1698107661,
      |  "usage_bytes": 123456,
      |  "last_active_at": 1698107661,
      |  "name": "test_vs",
      |  "status": "in_progress",
      |  "expires_at": 1698107651,
      |  "file_counts": {
      |    "in_progress": 0,
      |    "completed": 1,
      |    "cancelled": 2,
      |    "failed": 1,
      |    "total": 4
      |  },
      |  "metadata": {},
      |  "last_used_at": 1698107681
      |}""".stripMargin
}
