package sttp.openai.fixtures

object CompletionsFixture {
  val jsonSinglePromptResponse: String = """{
                     |  "id": "cmpl-75C628xoevz3eE8zsTFDumZ5wqwmY",
                     |  "object": "text_completion",
                     |  "created": 1681472494,
                     |  "model": "gpt-3.5-turbo-instruct",
                     |  "choices": [
                     |    {
                     |      "text": "\n\nThis is indeed a test.",
                     |      "index": 0,
                     |      "logprobs": null,
                     |      "finish_reason": "stop"
                     |    }
                     |  ],
                     |  "usage": {
                     |    "prompt_tokens": 5,
                     |    "completion_tokens": 8,
                     |    "total_tokens": 13
                     |  }
                     |}""".stripMargin

  val jsonMultiplePromptResponse: String = """{
                    |  "id":"cmpl-76D8UlnqOEkhVXu29nY7UPZFDTTlP",
                    |  "object":"text_completion",
                    |  "created":1681714818,
                    |  "model":"gpt-3.5-turbo-instruct",
                    |  "choices":[
                    |    {
                    |      "text":"\n\nThis is indeed a test",
                    |      "index":0,
                    |      "logprobs":null,
                    |      "finish_reason":"length"
                    |    },
                    |    {
                    |      "text":"\n\nYes, this is also",
                    |      "index":1,
                    |      "logprobs":null,
                    |      "finish_reason":"length"
                    |    }
                    |  ],
                    |  "usage":{
                    |    "prompt_tokens":11,
                    |    "completion_tokens":14,
                    |    "total_tokens":25
                    |  }
                    |}
                    |
                    |""".stripMargin

  val jsonSinglePromptRequest: String = """{
                              |  "model": "gpt-3.5-turbo-instruct",
                              |  "prompt": "Say this is a test",
                              |  "max_tokens": 7,
                              |  "temperature": 0,
                              |  "top_p": 1,
                              |  "n": 1,
                              |  "stop": "\n"
                              |}""".stripMargin

  val jsonMultiplePromptRequest: String = """{
                              |  "model": "gpt-3.5-turbo-instruct",
                              |  "prompt": ["Say this is a test", "Say this is also a test"],
                              |  "max_tokens": 7,
                              |  "temperature": 0,
                              |  "top_p": 1,
                              |  "n": 1,
                              |  "stop": "\n"
                              |}""".stripMargin
}
