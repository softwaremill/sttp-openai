package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.collection.immutable.ListMap
import sttp.apispec.{Schema, SchemaType}
import sttp.openai.fixtures
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.ChatRequestBody.ResponseFormat.JsonSchema

class JsonSchemaSpec extends AnyFlatSpec with Matchers with EitherValues {
  "Given string JSON schema" should "be properly serialized to Json" in {
    val schema = Schema(SchemaType.String)

    val jsonStringSchema = ujson.read(fixtures.JsonSchemaFixture.stringSchema)

    val serializedSchema = SnakePickle.writeJs(JsonSchema("testString", true, schema))

    serializedSchema shouldBe jsonStringSchema
  }

  "Given number JSON schema" should "be properly serialized to Json" in {
    val schema = Schema(SchemaType.Number)

    val jsonNumberSchema = ujson.read(fixtures.JsonSchemaFixture.numberSchema)

    val serializedSchema = SnakePickle.writeJs(JsonSchema("testNumber", true, schema))

    serializedSchema shouldBe jsonNumberSchema
  }

  "Given object JSON schema" should "be properly serialized to Json" in {
    val schema = Schema(SchemaType.Object)
      .copy(properties = ListMap("foo" -> Schema(SchemaType.String), "bar" -> Schema(SchemaType.Number)))

    val jsonObjectSchema = ujson.read(fixtures.JsonSchemaFixture.objectSchema)

    val serializedSchema = SnakePickle.writeJs(JsonSchema("testObject", true, schema))

    serializedSchema shouldBe jsonObjectSchema
  }

  "Given array JSON schema" should "be properly serialized to Json" in {
    val schema = Schema(SchemaType.Array).copy(items = Some(Schema(SchemaType.String)))

    val jsonArraySchema = ujson.read(fixtures.JsonSchemaFixture.arraySchema)

    val serializedSchema = SnakePickle.writeJs(JsonSchema("testArray", true, schema))

    serializedSchema shouldBe jsonArraySchema
  }
}
