package sttp.openai.requests.completions.chat

import sttp.openai.json.SnakePickle

/** @param id
  *   The ID of the tool call.
  * @param type
  *   The type of the tool. Currently, only function is supported.
  * @param function
  *   The function that the model called.
  */
case class ToolCall(id: String, `type`: String, function: FunctionCall)

object ToolCall {
  implicit val toolCallRW: SnakePickle.ReadWriter[ToolCall] = SnakePickle.macroRW[ToolCall]
}
