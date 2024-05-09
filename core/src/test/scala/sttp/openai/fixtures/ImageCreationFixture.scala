package sttp.openai.fixtures

object ImageCreationFixture {
  val jsonRequest: String = """{
      |"prompt": "cute fish",
      |"model": "dall-e-3",
      |"n": 1,
      |"size": "1024x1024",
      |"response_format": "url",
      |"user": "user1"
      |}""".stripMargin

  val jsonRequestDalle2: String = """{
      |"prompt": "cute fish",
      |"model": "dall-e-2",
      |"n": 1,
      |"size": "1024x1024",
      |"response_format": "url",
      |"user": "user1"
      |}""".stripMargin

  val jsonResponse: String = """{
      |"created": 1681893694,
      |"data": [
      |  {
      |    "url": "https://generated.image.url"
      |  }
      |]
      |}""".stripMargin
}
