package sttp.openai.fixtures

object AdminFixture {

  val jsonRequest: String =
    """{
        |  "name": "api_key_name"
        |}""".stripMargin

  val jsonResponse: String =
    """{
      |  "object": "organization.admin_api_key",
      |  "id": "key_xyz",
      |  "name": "New Admin Key",
      |  "redacted_value": "sk-admin...xyz",
      |  "created_at": 1711471533,
      |  "owner": {
      |    "type": "user",
      |    "object": "organization.user",
      |    "id": "user_123",
      |    "name": "John Doe",
      |    "created_at": 1711471533,
      |    "role": "owner"
      |  },
      |  "value": "sk-admin-1234abcd"
      |}""".stripMargin

}
