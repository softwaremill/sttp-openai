package sttp.openai.requests.completions.chat.message

import sttp.openai.json.SnakePickle
import ujson._

sealed trait ToolChoice

object ToolChoice {
  case class AsString(value: String) extends ToolChoice

  case class AsObject(function: Option[FunctionSpec] = None) extends ToolChoice

  implicit val asStringRW: SnakePickle.ReadWriter[AsString] = SnakePickle
    .readwriter[Value]
    .bimap[AsString](
      asString => Str(asString.value),
      json => AsString(json.str)
    )

  implicit val asObjectRW: SnakePickle.ReadWriter[AsObject] = SnakePickle
    .readwriter[Value]
    .bimap[AsObject](
      asObject => Obj("type" -> "function", "function" -> asObject.function.map(SnakePickle.writeJs(_)).getOrElse(ujson.Null)),
      json => AsObject(json.obj.get("function").map(SnakePickle.read[FunctionSpec](_)))
    )

  implicit val toolChoiceRW: SnakePickle.ReadWriter[ToolChoice] = SnakePickle
    .readwriter[Value]
    .bimap[ToolChoice](
      {
        case asString: AsString => SnakePickle.writeJs(asString)
        case asObject: AsObject => SnakePickle.writeJs(asObject)
      },
      json =>
        json.obj.get("type") match {
          case Some(Str("function")) => SnakePickle.read[AsObject](json)
          case _                     => SnakePickle.read[AsString](json)
        }
    )

  case class FunctionSpec(name: String)

  implicit val functionSpecRW: SnakePickle.ReadWriter[FunctionSpec] = SnakePickle.macroRW[FunctionSpec]

}
