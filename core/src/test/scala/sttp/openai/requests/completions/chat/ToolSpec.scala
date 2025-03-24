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
}
