package sttp.openai.fixtures

object CompletionsFixture {
  val jsonResponse: String = """{
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

  val jsonRequest: String = """{
                              |  "model": "text-davinci-003",
                              |  "prompt": "Say this is a test",
                              |  "max_tokens": 7,
                              |  "temperature": 0,
                              |  "top_p": 1,
                              |  "n": 1,
                              |  "stream": false,
                              |  "stop": "\n"
                              |}""".stripMargin
}
