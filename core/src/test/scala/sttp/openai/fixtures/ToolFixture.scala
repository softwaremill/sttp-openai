package sttp.openai.fixtures

object ToolFixture {

  val jsonToolCall: String =
    """{
      |  "description": "Books a flight for a passenger with full details",
      |  "name": "book_flight",
      |  "parameters": {
      |    "additionalProperties": false,
      |    "required": [
      |      "passenger",
      |      "departureCity",
      |      "destinationCity"
      |    ],
      |    "$schema": "http://json-schema.org/draft/2020-12/schema#",
      |    "$defs": {
      |      "Passenger": {
      |        "additionalProperties": false,
      |        "required": [
      |          "name",
      |          "age"
      |        ],
      |        "title": "Passenger",
      |        "type": "object",
      |        "properties": {
      |          "name": {
      |            "type": "string"
      |          },
      |          "age": {
      |            "type": "integer",
      |            "format": "int32"
      |          }
      |        }
      |      }
      |    },
      |    "title": "FlightDetails",
      |    "type": "object",
      |    "properties": {
      |      "passenger": {
      |        "$ref": "#/$defs/Passenger"
      |      },
      |      "departureCity": {
      |        "type": "string"
      |      },
      |      "destinationCity": {
      |        "type": "string"
      |      }
      |    }
      |  }
      |}""".stripMargin

}
