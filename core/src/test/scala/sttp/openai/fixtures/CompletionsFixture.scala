package sttp.openai.fixtures

object CompletionsFixture {
  val jsonSinglePromptResponse: String = """{
                     |  "id": "cmpl-75C628xoevz3eE8zsTFDumZ5wqwmY",
                     |  "object": "text_completion",
                     |  "created": 1681472494,
                     |  "model": "text-davinci-003",
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

  /**
   * Generated from:
   * curl http://localhost:11434/v1/completions -d '{
   *   "model": "llama3.2",
   *   "prompt": "Say Hello World as a haiku."
   * }'
   */
  val ollamaPromptResponse: String = """{
                     |  "id": "cmpl-712",
                     |  "object": "text_completion",
                     |  "created": 1733664264,
                     |  "model": "llama3.2",
                     |  "system_fingerprint": "fp_ollama",
                     |  "choices": [
                     |    {
                     |      "text": "Greeting coding dawn\n\"Hello, world!\" echoes bright\nProgramming's start",
                     |      "index": 0,
                     |      "finish_reason": "stop"
                     |    }
                     |  ],
                     |  "usage": {
                     |    "prompt_tokens": 33,
                     |    "completion_tokens": 17,
                     |    "total_tokens": 50
                     |  }
                     |}""".stripMargin

  val jsonMultiplePromptResponse: String = """{
                    |  "id":"cmpl-76D8UlnqOEkhVXu29nY7UPZFDTTlP",
                    |  "object":"text_completion",
                    |  "created":1681714818,
                    |  "model":"text-davinci-003",
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
                              |  "model": "text-davinci-003",
                              |  "prompt": "Say this is a test",
                              |  "max_tokens": 7,
                              |  "temperature": 0,
                              |  "top_p": 1,
                              |  "n": 1,
                              |  "stop": "\n"
                              |}""".stripMargin

  val jsonMultiplePromptRequest: String = """{
                              |  "model": "text-davinci-003",
                              |  "prompt": ["Say this is a test", "Say this is also a test"],
                              |  "max_tokens": 7,
                              |  "temperature": 0,
                              |  "top_p": 1,
                              |  "n": 1,
                              |  "stop": "\n"
                              |}""".stripMargin
}
