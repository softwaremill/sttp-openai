package sttp.openai.requests.completions.chat

import sttp.openai.OpenAIExceptions
import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.json.SnakePickle
import ujson.Str

/** @param role
  *   The role of the author of this message. One of [[Role]].
  * @param content
  *   The contents of the message.
  * @param name
  *   The name of the author of this message. May contain a-z, A-Z, 0-9, and underscores, with a maximum length of 64 characters.
  */
case class Message(role: Role, content: String, name: Option[String] = None)

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

  private val byRoleValue = values.map(role => role.value -> role).toMap

  implicit val roleRW: SnakePickle.ReadWriter[Role] = SnakePickle
    .readwriter[ujson.Value]
    .bimap[Role](
      role => SnakePickle.writeJs(role.value),
      jsonValue =>
        SnakePickle.read[ujson.Value](jsonValue) match {
          case Str(value) => byRoleValue.getOrElse(value, Custom(value))
          case e          => throw DeserializationOpenAIException(new Exception(s"Could not deserialize: $e"))
        }
    )
}
