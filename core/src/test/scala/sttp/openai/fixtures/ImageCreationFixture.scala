package sttp.openai.fixtures

object ImageCreationFixture {
  val jsonRequest: String = """{
      |"prompt": "cute fish",
      |"n": 1,
      |"size": "1024x1024",
      |"response_format": "test",
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
