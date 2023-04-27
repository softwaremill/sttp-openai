package sttp.openai.requests.completions.chat

import sttp.openai.json.{DeserializationException, SnakePickle}
import ujson.Str

/** @param role
  *   The role of the author of this message. One of `system`, `user`, or `assistant`.
  * @param content
  *   The contents of the message.
  */
case class Message(role: String, content: String, name: Option[String] = None)

object Message {
  implicit val messageRW: SnakePickle.ReadWriter[Message] = SnakePickle.macroRW[Message]
}

sealed abstract class Role(val value: String)
object Role {
  case object System extends Role("system")

  case object User extends Role("user")

  case object Assistant extends Role("assistant")

  case class Custom(customRole: String) extends Role(customRole)

  val values: Set[Role] = Set(System, User, Assistant)

  private val byName = values.map(role => (role.getClass.getSimpleName.stripSuffix("$") -> role)).toMap

  implicit val roleRW: SnakePickle.ReadWriter[Role] = SnakePickle.readwriter[ujson.Value].bimap[Role](
    {
      case System => SnakePickle.writeJs(System.value)
      case User => SnakePickle.writeJs(User.value)
      case Assistant => SnakePickle.writeJs(Assistant.value)
      case Custom(customRole) => SnakePickle.writeJs(customRole)
    },
    jsonValue => SnakePickle.read[ujson.Value](jsonValue) match {
      case Str(value) => byName.getOrElse(value, throw new DeserializationException(new Exception(s"Could not serialize $value")))
      case e => throw new DeserializationException(new Exception(s"Could not serialize: $e"))
    }
  )
}
