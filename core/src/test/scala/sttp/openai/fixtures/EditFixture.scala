package sttp.openai.fixtures

object EditFixture {

  val jsonResponse: String = """{
      |  "object": "edit",
      |  "created": 1681798630,
      |  "choices": [
      |    {
      |      "text": "What day of the week is it?",
      |      "index": 0
      |    }
      |  ],
      |  "usage": {
      |    "prompt_tokens": 25,
      |    "completion_tokens": 32,
      |    "total_tokens": 57
      |  }
      |}""".stripMargin

  val jsonRequest: String = """{
      |"model": "text-davinci-edit-001",
      |"input": "What day of the wek is it?",
      |"instruction": "Fix the spelling mistakes"
      |}""".stripMargin

}
