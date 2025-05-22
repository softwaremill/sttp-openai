package sttp.openai.requests.completions.chat

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.openai.fixtures.ToolFixture
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.message.Tool.SchematizedFunctionTool

class ToolSpec extends AnyFlatSpec with Matchers with EitherValues {
  case class Passenger(name: String, age: Int)

  case class FlightDetails(passenger: Passenger, departureCity: String, destinationCity: String)

  "Given SchematizedToolCall" should "be properly serialized to Json" in {
    import sttp.tapir.generic.auto._
    // given
    val functionTool =
      SchematizedFunctionTool[FlightDetails](name = "book_flight", description = "Books a flight for a passenger with full details")
    val expectedJson = ujson.read(ToolFixture.jsonToolCall)
    // when
    val serializedToolCall = SnakePickle.writeJs(functionTool)
    // then
    serializedToolCall shouldBe expectedJson
  }

  "Given FunctionTool with strict flag" should "serialize and deserialize properly" in {
    import sttp.openai.requests.completions.chat.message.Tool.FunctionTool
    // given
    val funcTool = FunctionTool(
      description = "Return greeting",
      name = "greet",
      parameters = Map("type" -> ujson.Str("object")),
      strict = Some(true)
    )

    val expectedJson = ujson.read(ToolFixture.jsonToolCallStrictTrue)

    // when
    val serialized = SnakePickle.writeJs(funcTool)
    serialized shouldBe expectedJson

    // and deserialization
    val deserialized = SnakePickle.read[FunctionTool](expectedJson)
    deserialized shouldBe funcTool
  }

  "Given SchematizedFunctionTool with strict flag" should "serialize and deserialize properly" in {
    import sttp.tapir.generic.auto._
    val tool = SchematizedFunctionTool[FlightDetails](
      name = "book_flight",
      description = "Books a flight for a passenger with full details",
      strict = Some(true)
    )

    val expectedJson = ujson.read(ToolFixture.jsonSchematizedToolCallStrictTrue)

    val serialized = SnakePickle.writeJs(tool)
    serialized shouldBe expectedJson

    val deserialized = SnakePickle.read[sttp.openai.requests.completions.chat.message.Tool.SchematizedFunctionTool](expectedJson)
    // Ensure strict flag is preserved
    deserialized.strict should contain(true)
    // Ensure serialisation after deserialization matches expected JSON (round-trip)
    SnakePickle.writeJs(deserialized) shouldBe expectedJson
  }
}
