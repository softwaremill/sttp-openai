package sttp.openai.requests.completions.chat.message

import sttp.openai.json.SnakePickle
import ujson._

sealed trait ToolChoice

object ToolChoice {
  case class ToolNone() extends ToolChoice
  case class ToolAuto() extends ToolChoice
  case class ToolFunction(name: String) extends ToolChoice

  implicit val toolNoneRW: SnakePickle.ReadWriter[ToolNone] = SnakePickle
    .readwriter[Value]
    .bimap[ToolNone](
      _ => Str("none"),
      _ => ToolNone()
    )

  implicit val toolAutoRW: SnakePickle.ReadWriter[ToolAuto] = SnakePickle
    .readwriter[Value]
    .bimap[ToolAuto](
      _ => Str("auto"),
      _ => ToolAuto()
    )

  implicit val toolFunctionRW: SnakePickle.ReadWriter[ToolFunction] = SnakePickle
    .readwriter[Value]
    .bimap[ToolFunction](
      toolFunction => Obj("type" -> "function", "function" -> Obj("name" -> toolFunction.name)),
      json => ToolFunction(json.obj("function")("name").str)
    )

  implicit val toolChoiceRW: SnakePickle.ReadWriter[ToolChoice] = SnakePickle
    .readwriter[Value]
    .bimap[ToolChoice](
      {
        case toolAuto: ToolAuto         => SnakePickle.writeJs(toolAuto)
        case toolNone: ToolNone         => SnakePickle.writeJs(toolNone)
        case toolFunction: ToolFunction => SnakePickle.writeJs(toolFunction)
      },
      {
        case json @ Str("none") => SnakePickle.read[ToolNone](json)
        case json @ Str("auto") => SnakePickle.read[ToolAuto](json)
        case json               => SnakePickle.read[ToolFunction](json)
      }
    )

  case class FunctionSpec(name: String)

  implicit val functionSpecRW: SnakePickle.ReadWriter[FunctionSpec] = SnakePickle.macroRW[FunctionSpec]

}
