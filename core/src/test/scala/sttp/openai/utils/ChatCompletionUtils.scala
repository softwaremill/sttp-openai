package sttp.openai.utils

import sttp.openai.requests.completions.chat.{FunctionCall, ToolCall}
import sttp.openai.requests.completions.chat.message._
import ujson._

object ChatCompletionUtils {
  def messages: Seq[Message] = systemMessages ++ userMessages ++ assistantMessages ++ toolMessages

  def systemMessages: Seq[Message.SystemMessage] =
    Seq(Message.SystemMessage("Hello!"), Message.SystemMessage("Hello!", Some("User")))

  def userMessages: Seq[Message.UserMessage] = {
    val parts = Seq(
      Content.TextContentPart("Hello!"),
      Content.ImageContentPart(Content.ImageUrl("https://i.imgur.com/2tj5rQE.jpg"))
    )
    val arrayMessage = Message.UserMessage(Content.ArrayContent(parts))
    val stringMessage = Message.UserMessage(Content.TextContent("Hello!"), Some("User"))

    Seq(stringMessage, arrayMessage)
  }

  def assistantMessages: Seq[Message.AssistantMessage] =
    Seq(
      Message.AssistantMessage("Hello!", Some("User"), Some(toolCalls)),
      Message.AssistantMessage("Hello!", Some("User")),
      Message.AssistantMessage("Hello!")
    )

  def toolMessages: Seq[Message.ToolMessage] =
    Seq(
      Message.ToolMessage("Hello!", "tool_call_id_1"),
      Message.ToolMessage("Hello!", "tool_call_id_2")
    )

  def tools: Seq[Tool] = {
    val function = Tool.FunctionCall(
      description = "Random description",
      name = "Random name",
      parameters = Map(
        "type" -> Str("object"),
        "properties" -> Obj(
          "location" -> Obj(
            "type" -> "string",
            "description" -> "The city and state e.g. San Francisco, CA"
          )
        ),
        "required" -> Arr("location")
      )
    )

    Seq(
      Tool(
        "object",
        function = function
      )
    )
  }

  def toolCalls: Seq[ToolCall] = {
    val functionCall: FunctionCall = FunctionCall(
      arguments = "args",
      name = "Fish"
    )

    Seq(
      ToolCall("tool_id_1", "function", functionCall),
      ToolCall("tool_id_2", "function", functionCall)
    )
  }
}
