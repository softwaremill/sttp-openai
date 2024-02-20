package sttp.openai.requests.threads.messages

import sttp.openai.json.SnakePickle

object ThreadMessagesRequestBody {

  /** @param role
    *   string Required The role of the entity that is creating the message. Currently only user is supported.
    * @param content
    *   string Required The content of the message.
    * @param file_ids
    *   array Optional Defaults to [] A list of File IDs that the message should use. There can be a maximum of 10 files attached to a
    *   message. Useful for tools like retrieval and code_interpreter that can access and use files.
    * @param metadata
    *   map Optional Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information
    *   about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters
    *   long.
    *
    * For more information please visit: [[https://platform.openai.com/docs/api-reference/messages/createMessage]]
    */
  case class CreateMessage(
      role: String,
      content: String,
      file_ids: Seq[String] = Seq.empty,
      metadata: Option[Map[String, String]] = None
  )

  object CreateMessage {
    implicit val completionBodyW: SnakePickle.Writer[CreateMessage] = SnakePickle.macroW[CreateMessage]
  }
}
