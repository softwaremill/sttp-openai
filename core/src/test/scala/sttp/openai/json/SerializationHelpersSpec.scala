package sttp.openai.json

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.json.SerializationHelpersSpec.TestObject
import ujson._

object SerializationHelpersSpec {
  sealed trait Base
  @upickle.implicits.key("test_object_key")
  case class TestObject(name: String, value: Int, optionalField: Option[String] = None) extends Base
}

class SerializationHelpersSpec extends AnyFlatSpec with Matchers {

  "withFlattenedDiscriminator" should "add discriminator field when writing object" in {
    // given
    val testObj = TestObject("test", 42, Some("optional"))
    implicit val testObjectW: SnakePickle.Writer[TestObject] = SnakePickle.macroW

    // when
    val serializedJson = SnakePickle.writeJs(testObj)

    // then
    val expectedJson = Obj(
      "type" -> Str("test_object_key"),
      "name" -> Str("test"),
      "value" -> Num(42),
      "optional_field" -> Str("optional")
    )
    serializedJson shouldBe expectedJson
  }

  it should "remove discriminator field when reading object" in {
    // given
    val inputJson = Obj(
      "type" -> Str("test_object_key"),
      "name" -> Str("test"),
      "value" -> Num(42),
      "optional_field" -> Str("optional")
    )
    implicit val testObjectR: SnakePickle.Reader[TestObject] = SnakePickle.macroR

    // when
    val deserializedObj = SnakePickle.read[TestObject](inputJson)

    // then
    val expectedObj = TestObject("test", 42, Some("optional"))
    deserializedObj shouldBe expectedObj
  }

}
