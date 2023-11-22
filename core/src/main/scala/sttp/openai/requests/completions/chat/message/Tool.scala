package sttp.openai.requests.completions.chat.message

import sttp.openai.json.SnakePickle
import ujson._

sealed trait Tool

object Tool {

  /** @param description
    *   A description of what the function does, used by the model to choose when and how to call the function.
    * @param name
    *   The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
    * @param parameters
    *   The parameters the functions accepts, described as a JSON Schema object
    */
  case class FunctionTool(description: String, name: String, parameters: Map[String, Value]) extends Tool

  implicit val functionToolRW: SnakePickle.ReadWriter[FunctionTool] = SnakePickle
    .readwriter[Value]
    .bimap[FunctionTool](
      functionTool => Obj("description" -> functionTool.description, "name" -> functionTool.name, "parameters" -> functionTool.parameters),
      json => FunctionTool(json("description").str, json("name").str, json("parameters").obj.toMap)
    )

  implicit val toolRW: SnakePickle.ReadWriter[Tool] = SnakePickle
    .readwriter[Value]
    .bimap[Tool](
      { case functionTool: FunctionTool =>
        Obj("type" -> "function", "function" -> SnakePickle.writeJs(functionTool))
      },
      json => SnakePickle.read[FunctionTool](json("function"))
    )
}
