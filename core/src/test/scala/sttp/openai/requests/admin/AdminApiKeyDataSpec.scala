package sttp.openai.requests.admin

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures.AdminFixture
import sttp.openai.json.{SnakePickle, SttpUpickleApiExtension}

class AdminApiKeyDataSpec extends AnyFlatSpec with Matchers with EitherValues {
  "Given create admin api key request as case class" should "be properly serialized to Json" in {
    // given
    val givenRequest = AdminApiKeyRequestBody(
      name = "api_key_name"
    )
    val jsonRequest: ujson.Value = ujson.read(AdminFixture.jsonRequest)
    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)
    // then
    serializedJson shouldBe jsonRequest
  }

  "Given create admin api key response as Json" should "be properly deserialized to case class" in {
    // given
    val jsonResponse = AdminFixture.jsonResponse
    val expectedResponse: AdminApiKeyResponse = AdminApiKeyResponse(
      id = "key_xyz",
      name = "New Admin Key",
      redactedValue = "sk-admin...xyz",
      createdAt = 1711471533,
      owner = Owner(
        `type` = "user",
        `object` = "organization.user",
        id = "user_123",
        name = "John Doe",
        createdAt = 1711471533,
        role = "owner"
      ),
      value = Some("sk-admin-1234abcd")
    )
    // when
    val deserializedJsonResponse: Either[Exception, AdminApiKeyResponse] =
      SttpUpickleApiExtension.deserializeJsonSnake[AdminApiKeyResponse].apply(jsonResponse)
    // then
    deserializedJsonResponse.value shouldBe expectedResponse
  }

}
