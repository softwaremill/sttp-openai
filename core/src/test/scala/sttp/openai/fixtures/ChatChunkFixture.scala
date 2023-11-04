package sttp.openai.fixtures

object ChatChunkFixture {

  val jsonResponse: String =
    """{
      |   "id": "chatcmpl-76FxnKOjnPkDVYTAQ1wK8iUNFJPvR",
      |   "object": "chat.completion",
      |   "created": 1681725687,
      |   "model": "gpt-3.5-turbo-0301",
      |   "choices": [
      |     {
      |       "delta": {
      |         "role": "assistant",
      |         "content": "  Hi",
      |         "function_call": {
      |           "arguments": "args",
      |           "name": "Fish"
      |         }
      |       },
      |       "finish_reason": "stop",
      |       "index": 0
      |     }
      |   ]
      |}""".stripMargin

}
