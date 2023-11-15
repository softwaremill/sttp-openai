package sttp.openai.fixtures

object ChatFixture {

  val jsonRequest: String =
    """{
      |  "messages": [
      |    {
      |      "$type": "sttp.openai.requests.completions.chat._chat_request_body._message._user_message",
      |      "content": "Hello!",
      |      "role": "user"
      |    },
      |    {
      |      "$type": "sttp.openai.requests.completions.chat._chat_request_body._message._user_message",
      |      "content": [
      |        {
      |          "$type": "sttp.openai.requests.completions.chat._chat_request_body._message._content._text_content_part",
      |          "type": "object",
      |          "text": "Hello!"
      |        },
      |        {
      |          "$type": "sttp.openai.requests.completions.chat._chat_request_body._message._content._image_content_part",
      |          "type": "object",
      |          "image": "https://i.imgur.com/tj5G2rO.jpg"
      |        }
      |      ],
      |      "role": "user"
      |    }
      |  ],
      |  "model": "gpt-3.5-turbo",
      |  "frequency_penalty": 0,
      |  "max_tokens": 7,
      |  "n": 1,
      |  "presence_penalty": 0,
      |  "stop": "\n",
      |  "temperature": 1,
      |  "top_p": 1,
      |  "tools": [
      |    {
      |      "type": "object",
      |      "function": {
      |        "description": "Random description",
      |        "name": "Random name",
      |        "parameters": {
      |          "type": "object",
      |          "properties": {
      |            "location": {
      |              "type": "string",
      |              "description": "The city and state e.g. San Francisco, CA"
      |            }
      |          },
      |          "required": ["location"]
      |        }
      |      }
      |    }
      |  ],
      |  "tool_choice": {
      |    "$type": "sttp.openai.requests.completions.chat._chat_request_body._tool_choice._as_object",
      |    "type": "object",
      |    "function": {
      |      "name": "function"
      |    }
      |  },
      |  "user": "testUser"
      |}
      |""".stripMargin

  val jsonResponse: String =
    """{
      |  "id": "chatcmpl-76FxnKOjnPkDVYTAQ1wK8iUNFJPvR",
      |  "object": "chat.completion",
      |  "created": 1681725687,
      |  "model": "gpt-3.5-turbo-0301",
      |  "system_fingerprint": "systemFingerprint",
      |  "usage": {
      |    "prompt_tokens": 10,
      |    "completion_tokens": 10,
      |    "total_tokens": 20
      |  },
      |  "choices": [
      |    {
      |      "message": {
      |        "role": "assistant",
      |        "content": "Hi there! How can I assist you today?",
      |        "tool_calls": [
      |         {
      |           "id": "tool_id_1",
      |           "type": "function",
      |           "function": {
      |             "arguments": "args",
      |             "name": "Fish"
      |           }
      |         },
      |         {
      |           "id": "tool_id_2",
      |           "type": "function",
      |           "function": {
      |             "arguments": "args",
      |             "name": "Fish"
      |           }
      |         }
      |        ]
      |      },
      |      "finish_reason": "stop",
      |      "index": 0
      |    }
      |  ]
      |}
      |""".stripMargin

}
