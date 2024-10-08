package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.ChatRequestBody._
import ResponseFormat._
import sttp.openai.requests.completions.chat.message.{Content, Message}

class ChatStructuredOutputSpec extends AnyFlatSpec with Matchers with EitherValues {

  "ResponseFormat" should "serialize and deserialize correctly" in {
    val testCases = Table(
      ("format", "expectedJson"),
      (ResponseFormat.JsonSchema(schemaObj, true), ujson.Obj("type" -> "json_schema", "json_schema" -> ujson.Obj("schema" -> schemaObj, "strict" -> true))),
      (ResponseFormat.JsonSchema(schemaObj, false), ujson.Obj("type" -> "json_schema", "json_schema" -> ujson.Obj("schema" -> schemaObj))),
      (ResponseFormat.Text, ujson.Obj("type" -> "text")),
      (ResponseFormat.JsonObject, ujson.Obj("type" -> "json_object"))
    )

    forAll(testCases) { (format: ResponseFormat, expectedJson) =>
      val serialized = SnakePickle.write(format)
      val deserialized = SnakePickle.read[ResponseFormat](serialized)

      deserialized shouldBe format
      ujson.read(serialized) shouldBe expectedJson
    }
  }

  "ChatRequestBody" should "include JsonSchema response format when serialized" in {
    val chatBody = ChatBody(
      messages = Seq(Message.UserMessage(Content.TextContent("Generate a person's details"))),
      model = ChatCompletionModel.GPT4o,
      responseFormat = Some(ResponseFormat.JsonSchema(schemaObj, true))
    )

    val serialized = SnakePickle.write(chatBody)
    val json = ujson.read(serialized)

    json("response_format") shouldBe ujson.Obj(
      "type" -> "json_schema",
      "json_schema" -> ujson.Obj(
        "schema" -> schemaObj,
        "strict" -> true
      )
    )
  }

  it should "handle JsonSchema with strict set to false" in {
    val chatBody = ChatBody(
      messages = Seq(Message.UserMessage(Content.TextContent("Generate a person's details"))),
      model = ChatCompletionModel.GPT4o,
      responseFormat = Some(ResponseFormat.JsonSchema(schemaObj, false))
    )

    val serialized = SnakePickle.write(chatBody)
    val json = ujson.read(serialized)

    json("response_format") shouldBe ujson.Obj(
      "type" -> "json_schema",
      "json_schema" -> ujson.Obj(
        "schema" -> schemaObj,
      )
    )
  }

  private lazy val schemaObj = ujson.Obj(
    "type" -> "object",
    "properties" -> ujson.Obj(
      "name" -> ujson.Obj("type" -> "string"),
      "age" -> ujson.Obj("type" -> "integer")
    ),
    "required" -> ujson.Arr("name", "age"),
    "additionalProperties" -> false
  )
}
