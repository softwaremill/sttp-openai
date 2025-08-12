package sttp.openai.requests.completions.chat.message

import sttp.openai.json.{SerializationHelpers, SnakePickle}
import ujson._

sealed trait ToolChoice

object ToolChoice {

  /** Means that the model will not call a function and instead generates a message. */
  case object ToolNone extends ToolChoice

  /** Means the model can pick between generating a message or calling a function. */
  case object ToolAuto extends ToolChoice

  case object ToolRequired extends ToolChoice

  /** Means the model will call a function. */
  case class ToolFunction(name: String) extends ToolChoice

  case class CustomTool(name: String) extends ToolChoice

  object AllowedTools {
    case object Auto extends Mode
    case object Required extends Mode
    sealed trait Mode
    implicit val modeRW: SnakePickle.Writer[Mode] = SnakePickle.writer[Value].comap[Mode]({
      case Auto => Str("auto")
      case Required => Str("required")
    })

  }
  case class AllowedTools(mode: AllowedTools.Mode, tools: List[Tool]) extends ToolChoice

  // Utility function to reduce boilerplate for simple string-based case objects
  private def stringCaseObject[T](value: String): SnakePickle.Writer[T] =
    SnakePickle
      .writer[Value]
      .comap(_ => Str(value))

  implicit val toolNoneRW: SnakePickle.Writer[ToolNone.type] = stringCaseObject("none")
  implicit val toolAutoRW: SnakePickle.Writer[ToolAuto.type] = stringCaseObject("auto")

  implicit val toolRequiredRW: SnakePickle.Writer[ToolRequired.type] = stringCaseObject("required")

  implicit val toolFunctionRW: SnakePickle.Writer[ToolFunction] =
    SerializationHelpers.withNestedDiscriminator("function", "function")(SnakePickle.macroW)
  implicit val allowedToolsRW: SnakePickle.Writer[AllowedTools] =
    SerializationHelpers.withNestedDiscriminator("allowed_tools", "allowed_tools")(SnakePickle.macroW)

  implicit val customToolRW: SnakePickle.Writer[CustomTool] =
    SerializationHelpers.withNestedDiscriminator("custom", "custom")(SnakePickle.macroW)

  implicit val toolChoiceRW: SnakePickle.Writer[ToolChoice] = SnakePickle
    .writer[Value]
    .comap[ToolChoice] {
      case toolAuto: ToolAuto.type         => SnakePickle.writeJs(toolAuto)
      case toolNone: ToolNone.type         => SnakePickle.writeJs(toolNone)
      case toolRequired: ToolRequired.type => SnakePickle.writeJs(toolRequired)
      case toolFunction: ToolFunction      => SnakePickle.writeJs(toolFunction)
      case customTool: CustomTool          => SnakePickle.writeJs(customTool)
      case allowedTools: AllowedTools      => SnakePickle.writeJs(allowedTools)
    }

}
