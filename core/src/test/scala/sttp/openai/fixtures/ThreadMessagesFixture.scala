package sttp.openai.fixtures

object ThreadMessagesFixture {

  val jsonCreateMessageRequest: String =
    """{
      |  "role": "user",
      |  "content": "How does AI work? Explain it in simple terms."
      |}""".stripMargin

  val jsonCreateMessageResponse: String =
    """
      |{
      |  "id": "msg_abc123",
      |  "object": "thread.message",
      |  "created_at": 1699017614 ,
      |  "thread_id": "thread_abc123",
      |  "role": "user",
      |  "content": [
      |    {
      |      "type": "text",
      |      "text": {
      |        "value": "How does AI work? Explain it in simple terms.",
      |        "annotations": []
      |      }
      |    }
      |  ],
      |  "file_ids": [],
      |  "assistant_id": null,
      |  "run_id": null,
      |  "metadata": {}
      |}
      |""".stripMargin

  val jsonListMessagesResponse: String =
    """
      |{
      |  "object": "list",
      |  "data": [
      |    {
      |      "id": "msg_abc123",
      |      "object": "thread.message",
      |      "created_at": 1699016383,
      |      "thread_id": "thread_abc123",
      |      "role": "user",
      |      "content": [
      |        {
      |          "type": "text",
      |          "text": {
      |            "value": "How does AI work? Explain it in simple terms.",
      |            "annotations": []
      |          }
      |        }
      |      ],
      |      "file_ids": [],
      |      "assistant_id": null,
      |      "run_id": null,
      |      "metadata": {}
      |    },
      |    {
      |      "id": "msg_abc456",
      |      "object": "thread.message",
      |      "created_at": 1699016383,
      |      "thread_id": "thread_abc123",
      |      "role": "user",
      |      "content": [
      |        {
      |          "type": "text",
      |          "text": {
      |            "value": "Hello, what is AI?",
      |            "annotations": []
      |          }
      |        }
      |      ],
      |      "file_ids": [
      |        "file-abc123"
      |      ],
      |      "assistant_id": null,
      |      "run_id": null,
      |      "metadata": {}
      |    }
      |  ],
      |  "first_id": "msg_abc123",
      |  "last_id": "msg_abc456",
      |  "has_more": false
      |}
      |""".stripMargin

  val jsonListMessageFilesResponse: String =
    """
      |{
      |  "object": "list",
      |  "data": [
      |    {
      |      "id": "file-abc123",
      |      "object": "thread.message.file",
      |      "created_at": 1699061776,
      |      "message_id": "msg_abc123"
      |    },
      |    {
      |      "id": "file-abc123",
      |      "object": "thread.message.file",
      |      "created_at": 1699061776,
      |      "message_id": "msg_abc123"
      |    }
      |  ],
      |  "first_id": "file-abc123",
      |  "last_id": "file-abc123",
      |  "has_more": false
      |}
      |""".stripMargin

  val jsonRetrieveMessageResponse: String =
    """
      |{
      |  "id": "msg_abc123",
      |  "object": "thread.message",
      |  "created_at": 1699017614,
      |  "thread_id": "thread_abc123",
      |  "role": "user",
      |  "content": [
      |    {
      |      "type": "text",
      |      "text": {
      |        "value": "How does AI work? Explain it in simple terms.",
      |        "annotations": []
      |      }
      |    }
      |  ],
      |  "file_ids": [],
      |  "assistant_id": null,
      |  "run_id": null,
      |  "metadata": {}
      |}
      |""".stripMargin

  val jsonRetrieveMessageFileResponse: String =
    """
      |{
      |  "id": "file-abc123",
      |  "object": "thread.message.file",
      |  "created_at": 1699061776,
      |  "message_id": "msg_abc123"
      |}
      |""".stripMargin

  val jsonModifyMessageResponse: String =
    """
      |{
      |  "id": "msg_abc123",
      |  "object": "thread.message",
      |  "created_at": 1699017614,
      |  "thread_id": "thread_abc123",
      |  "role": "user",
      |  "content": [
      |    {
      |      "type": "text",
      |      "text": {
      |        "value": "How does AI work? Explain it in simple terms.",
      |        "annotations": []
      |      }
      |    }
      |  ],
      |  "file_ids": [],
      |  "assistant_id": null,
      |  "run_id": null,
      |  "metadata": {
      |    "modified": "true",
      |    "user": "abc123"
      |  }
      |}
      |""".stripMargin
}
