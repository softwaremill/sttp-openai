package sttp.openai.json

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.json.SerializationHelpers.DiscriminatorField
import sttp.openai.json.SnakePickle
import ujson._

import scala.reflect.ClassTag

class SerializationHelpersSpec extends AnyFlatSpec with Matchers {

  sealed trait Core
  case class TestObject(name: String, value: Int, optionalField: Option[String] = None) extends Core
  case class SimpleData(message: String) extends Core
  case class EmptyObject() extends Core

  "withFlattenedDiscriminator" should "add discriminator field when writing object" in {
    // given
    val testObj = TestObject("test", 42, Some("optional"))
    val discriminatorField = DiscriminatorField("type")
    implicit val flattenedRW: SnakePickle.Writer[TestObject] =
      SerializationHelpers.withFlattenedDiscriminator[TestObject](discriminatorField, "test_object")(SnakePickle.macroRW)

    // when
    val serializedJson = SnakePickle.writeJs(testObj)

    // then
    val expectedJson = Obj(
      "type" -> Str("test_object"),
      "name" -> Str("test"),
      "value" -> Num(42),
      "optional_field" -> Str("optional")
    )
    serializedJson shouldBe expectedJson
  }

  it should "remove discriminator field when reading object" in {
    //given
    val inputJson = Obj(
      "type" -> Str("test_object"),
      "name" -> Str("test"),
      "value" -> Num(42),
      "optional_field" -> Str("optional")
    )
    val discriminatorField = DiscriminatorField("type")
    implicit val flattenedRW: SnakePickle.Reader[TestObject] = SerializationHelpers.withFlattenedDiscriminatorReader[TestObject](discriminatorField, SnakePickle.macroRW)

    //when
    val deserializedObj = SnakePickle.read[TestObject](inputJson)

    //then
    val expectedObj = TestObject("test", 42, Some("optional"))
    deserializedObj shouldBe expectedObj
  }

//  it should "filter out null values from optional fields when writing" in {
//    //given
//    val discriminatorField = DiscriminatorField("type")
//    val testObj = TestObject("test", 42, None) // None should become null and be filtered
//    implicit val flattenedRW: SnakePickle.Writer[TestObject] = SerializationHelpers.withFlattenedDiscriminator[TestObject](discriminatorField, "test_object")(SnakePickle.macroRW)
//
//    //when
//    val serializedJson = SnakePickle.writeJs(testObj)
//
//    //then
//    val expectedJson = Obj(
//      "type" -> Str("test_object"),
//      "name" -> Str("test"),
//      "value" -> Num(42)
//      // optional_field should be filtered out because it was null
//    )
//    serializedJson shouldBe expectedJson
//  }
//
//  it should "filter out $type fields when writing" in {
//    //given
//    val testObj = TestObject("test", 42, Some("optional"))
//    implicit val flattenedRW: SnakePickle.ReadWriter[TestObject] = SerializationHelpers.withFlattenedDiscriminator[TestObject]("type", "test_object")(SnakePickle.macroRW)
//
//    //when
//    val serializedJson = SnakePickle.writeJs(testObj)
//
//    //then
//    // Verify that no $type field is present in the output
//    serializedJson.obj.keys should not contain "$type"
//    serializedJson.obj should contain("type" -> Str("test_object"))
//  }
//
//  it should "perform round-trip serialization correctly" in {
//    //given
//    val originalObj = TestObject("test", 42, Some("optional"))
//    implicit val flattenedRW: SnakePickle.ReadWriter[TestObject] = SerializationHelpers.withFlattenedDiscriminator[TestObject]("type", "test_object")(SnakePickle.macroRW)
//
//    //when
//    val serializedJson = SnakePickle.writeJs(originalObj)
//    val deserializedObj = SnakePickle.read[TestObject](serializedJson)
//
//    //then
//    deserializedObj shouldBe originalObj
//  }
//
////  it should "handle round-trip with simple discriminator test" in {
////    //given
////    case class Simple(id: Int, name: String)
////    implicit val simpleRW: SnakePickle.ReadWriter[Simple] = SnakePickle.macroRW
////    implicit val flattenedRW: SnakePickle.ReadWriter[Simple] = SerializationHelpers.withFlattenedDiscriminator[Simple]("type", "simple")(simpleRW)
////
////    val originalObj = Simple(1, "test")
////
////    //when
////    val serializedJson = SnakePickle.writeJs(originalObj)
////    val deserializedObj = SnakePickle.read[Simple](serializedJson)
////
////    //then
////    deserializedObj shouldBe originalObj
////    // Verify discriminator is in serialized JSON
////    serializedJson.obj should contain("type" -> Str("simple"))
////  }
//
//  it should "handle non-object values by wrapping them in an object with discriminator" in {
//    //given
//    val simpleData = SimpleData("hello")
//    implicit val flattenedRW: SnakePickle.ReadWriter[SimpleData] = SerializationHelpers.withFlattenedDiscriminator[SimpleData]("type", "simple")(SnakePickle.macroRW)
//
//    //when
//    val serializedJson = SnakePickle.writeJs(simpleData)
//
//    //then
//    // Should contain the discriminator field and the snake_case field
//    serializedJson.obj should contain("type" -> Str("simple"))
//    serializedJson.obj should contain("message" -> Str("hello"))
//  }
//
//  it should "work with different discriminator field names and values" in {
//    //given
//    val testObj = TestObject("test", 42, Some("optional"))
//    implicit val flattenedRW: SnakePickle.ReadWriter[TestObject] = SerializationHelpers.withFlattenedDiscriminator[TestObject]("category", "example_type")(SnakePickle.macroRW)
//
//    //when
//    val serializedJson = SnakePickle.writeJs(testObj)
//    val deserializedObj = SnakePickle.read[TestObject](serializedJson)
//
//    //then
//    serializedJson.obj should contain("category" -> Str("example_type"))
//    serializedJson.obj should not contain "type"
//    deserializedObj shouldBe testObj
//  }
//
//  it should "preserve all object fields except discriminator when reading" in {
//    //given
//    val inputJson = Obj(
//      "type" -> Str("test_object"),
//      "name" -> Str("test"),
//      "value" -> Num(42),
//      "optional_field" -> Str("optional"),
//      "extra_field" -> Str("should_be_ignored") // This will be ignored during deserialization due to case class structure
//    )
//    implicit val flattenedRW: SnakePickle.ReadWriter[TestObject] = SerializationHelpers.withFlattenedDiscriminator[TestObject]("type", "test_object")(SnakePickle.macroRW)
//
//    //when
//    val deserializedObj = SnakePickle.read[TestObject](inputJson)
//
//    //then
//    val expectedObj = TestObject("test", 42, Some("optional"))
//    deserializedObj shouldBe expectedObj
//  }
//
//  it should "handle empty objects" in {
//    //given
//    val emptyObj = EmptyObject()
//    implicit val flattenedRW: SnakePickle.ReadWriter[EmptyObject] = SerializationHelpers.withFlattenedDiscriminator[EmptyObject]("type", "empty")(SnakePickle.macroRW)
//
//    //when
//    val serializedJson = SnakePickle.writeJs(emptyObj)
//    val deserializedObj = SnakePickle.read[EmptyObject](serializedJson)
//
//    //then
//    serializedJson.obj should contain("type" -> Str("empty"))
//    serializedJson.obj.size shouldBe 1 // Only the discriminator field
//    deserializedObj shouldBe emptyObj
//  }
}
