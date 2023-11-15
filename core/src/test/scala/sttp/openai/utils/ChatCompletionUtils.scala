package sttp.openai.utils

import sttp.openai.requests.completions.chat.{ChatRequestBody, FunctionCall, Role, ToolCall}
import ujson._
import ChatRequestBody._
import ChatRequestBody.Message._

object ChatCompletionUtils {
  def userMessages: Seq[ChatRequestBody.Message] = {
    val parts = Seq(
      Content.TextContentPart("object", "Hello!"),
      Content.ImageContentPart("object", "https://i.imgur.com/tj5G2rO.jpg")
    )
    val arrayMessage = UserMessage(Content.ArrayContent(parts), Role.User)
    val stringMessage = UserMessage(Content.TextContent("Hello!"), Role.User)

    Seq(stringMessage, arrayMessage)
  }

  def tools: Seq[ChatRequestBody.Tool] = {
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
