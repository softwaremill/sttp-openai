package sttp.openai.requests.assistants

import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.message.Tool

object AssistantsRequestBody {

  /** @param model
    *   ID of the model to use. You can use the List models API to see all of your available models, or see our Model overview for
    *   descriptions of them.
    *
    * @param name
    *   The name of the assistant. The maximum length is 256 characters.
    *
    * @param description
    *   The description of the assistant. The maximum length is 512 characters.
    *
    * @param instructions
    *   The system instructions that the assistant uses. The maximum length is 32768 characters.
    *
    * @param tools
    *   A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant. Tools can be of types code_interpreter,
    *   retrieval, or function.
    *
    * @param file_ids
    *   A list of file IDs attached to this assistant. There can be a maximum of 20 files attached to the assistant. Files are ordered by
    *   their creation date in ascending order.
    *
    * @param metadata
    *   Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object
    *   in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
    *
    * For more information please visit: [[https://platform.openai.com/docs/api-reference/assistants/createAssistant]]
    */
  case class CreateAssistantBody(
      model: String,
      name: Option[String] = None,
      description: Option[String] = None,
      instructions: Option[String] = None,
      tools: Seq[Tool] = Seq.empty,
      file_ids: Seq[String] = Seq.empty,
      metadata: Option[Map[String, String]] = None
  )
  object CreateAssistantBody {
    implicit val createAssistantBodyW: SnakePickle.Writer[CreateAssistantBody] = SnakePickle.macroW[CreateAssistantBody]
  }

  /** @param fileId
    *   A File ID (with purpose="assistants") that the assistant should use. Useful for tools like retrieval and code_interpreter that can
    *   access files.
    *
    * For more information please visit: [[https://platform.openai.com/docs/api-reference/assistants/createAssistantFile]]
    */
  case class CreateAssistantFileBody(fileId: String)

  object CreateAssistantFileBody {
    implicit val createAssistantFileBodyW: SnakePickle.Writer[CreateAssistantFileBody] = SnakePickle.macroW[CreateAssistantFileBody]
  }

  /** @param model
    *   ID of the model to use. You can use the List models API to see all of your available models, or see our Model overview for
    *   descriptions of them.
    *
    * @param name
    *   The name of the assistant. The maximum length is 256 characters.
    *
    * @param description
    *   The description of the assistant. The maximum length is 512 characters.
    *
    * @param instructions
    *   The system instructions that the assistant uses. The maximum length is 32768 characters.
    *
    * @param tools
    *   A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant. Tools can be of types code_interpreter,
    *   retrieval, or function.
    *
    * @param fileIds
    *   A list of File IDs attached to this assistant. There can be a maximum of 20 files attached to the assistant. Files are ordered by
    *   their creation date in ascending order. If a file was previously attached to the list but does not show up in the list, it will be
    *   deleted from the assistant.
    *
    * @param metadata
    *   Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object
    *   in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
    *
    * For more information please visit: [[https://platform.openai.com/docs/api-reference/assistants/modifyAssistant]]
    */
  case class ModifyAssistantBody(
      model: Option[String] = None,
      name: Option[String] = None,
      description: Option[String] = None,
      instructions: Option[String] = None,
      tools: Seq[Tool] = Seq.empty,
      fileIds: Seq[String] = Seq.empty,
      metadata: Map[String, String] = Map.empty
  )

  object ModifyAssistantBody {
    implicit val modifyAssistantBodyW: SnakePickle.Writer[ModifyAssistantBody] = SnakePickle.macroW[ModifyAssistantBody]
  }
}
