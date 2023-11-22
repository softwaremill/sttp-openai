package sttp.openai.requests.completions.chat.message

import sttp.openai.json.SnakePickle
import ujson._

sealed trait ToolChoice

object ToolChoice {

  /** Means that the model will not call a function and instead generates a message. */
  case object ToolNone extends ToolChoice

  /** Means the model can pick between generating a message or calling a function. */
  case object ToolAuto extends ToolChoice

  /** Means the model will call a function. */
  case class ToolFunction(name: String) extends ToolChoice

  implicit val toolNoneRW: SnakePickle.ReadWriter[ToolNone.type] = SnakePickle
    .readwriter[Value]
    .bimap[ToolNone.type](
      _ => Str("none"),
      _ => ToolNone
    )

  implicit val toolAutoRW: SnakePickle.ReadWriter[ToolAuto.type] = SnakePickle
    .readwriter[Value]
    .bimap[ToolAuto.type](
      _ => Str("auto"),
      _ => ToolAuto
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
        case toolAuto: ToolAuto.type    => SnakePickle.writeJs(toolAuto)
        case toolNone: ToolNone.type    => SnakePickle.writeJs(toolNone)
        case toolFunction: ToolFunction => SnakePickle.writeJs(toolFunction)
      },
      {
        case json @ Str("none") => SnakePickle.read[ToolNone.type](json)
        case json @ Str("auto") => SnakePickle.read[ToolAuto.type](json)
        case json               => SnakePickle.read[ToolFunction](json)
      }
    )

  case class FunctionSpec(name: String)

  implicit val functionSpecRW: SnakePickle.ReadWriter[FunctionSpec] = SnakePickle.macroRW[FunctionSpec]

}
