package sttp.openai.requests.completions.chat.message

import sttp.openai.json.SnakePickle
import ujson.Value

/** @param type
  *   The type of the tool. Currently, only function is supported.
  */
case class Tool(`type`: String, function: Tool.FunctionCall)

object Tool {
  implicit val toolRW: SnakePickle.ReadWriter[Tool] = SnakePickle.macroRW[Tool]

  /** @param description
    *   A description of what the function does, used by the model to choose when and how to call the function.
    * @param name
    *   The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
    * @param parameters
    *   The parameters the functions accepts, described as a JSON Schema object
    */
  case class FunctionCall(description: String, name: String, parameters: Map[String, Value])

  implicit val functionCallRW: SnakePickle.ReadWriter[FunctionCall] = SnakePickle.macroRW[FunctionCall]
}
