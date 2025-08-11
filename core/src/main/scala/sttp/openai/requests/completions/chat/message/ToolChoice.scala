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
  }
  case class AllowedTools(mode: AllowedTools.Mode, tools: List[Tool])

  // Utility function to reduce boilerplate for simple string-based case objects
  private def stringCaseObject[T](value: String): SnakePickle.Writer[T] =
    SnakePickle
      .writer[Value]
      .comap(_ => Str(value))

  implicit val toolNoneRW: SnakePickle.Writer[ToolNone.type] = stringCaseObject("none")
  implicit val toolAutoRW: SnakePickle.Writer[ToolAuto.type] = stringCaseObject("auto")

  implicit val toolRequiredRW: SnakePickle.Writer[ToolRequired.type] = stringCaseObject("required")

  implicit val toolFunctionRW: SnakePickle.Writer[ToolFunction] = SerializationHelpers.withNestedDiscriminator("function", "function")(SnakePickle.macroW)

  // Add missing ReadWriter for CustomTool
  implicit val customToolRW: SnakePickle.ReadWriter[CustomTool] = SnakePickle
    .readwriter[Value]
    .bimap[CustomTool](
      customTool => Obj("type" -> "custom", "name" -> customTool.name),
      json => CustomTool(json.obj("name").str)
    )

  implicit val toolChoiceRW: SnakePickle.Writer[ToolChoice] = SnakePickle
    .readwriter[Value]
    .bimap[ToolChoice](
      {
        case toolAuto: ToolAuto.type        => SnakePickle.writeJs(toolAuto)
        case toolNone: ToolNone.type        => SnakePickle.writeJs(toolNone)
        case toolRequired: ToolRequired.type => SnakePickle.writeJs(toolRequired)
        case toolFunction: ToolFunction     => SnakePickle.writeJs(toolFunction)
        case customTool: CustomTool         => SnakePickle.writeJs(customTool)
      },
      {
        case json @ Str("none")     => SnakePickle.read[ToolNone.type](json)
        case json @ Str("auto")     => SnakePickle.read[ToolAuto.type](json)
        case json @ Str("required") => SnakePickle.read[ToolRequired.type](json)
        case json: Obj if json.obj.contains("function") => SnakePickle.read[ToolFunction](json)
        case json: Obj if json.obj.get("type").contains(Str("custom")) => SnakePickle.read[CustomTool](json)
        case json => throw new IllegalArgumentException(s"Unknown tool choice format: $json")
      }
    )


}
