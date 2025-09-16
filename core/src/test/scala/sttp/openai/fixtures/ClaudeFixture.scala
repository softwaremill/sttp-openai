package sttp.openai.fixtures

import sttp.openai.requests.claude.ClaudeRequestBody.{ClaudeMessageRequest, ClaudeModel}
import sttp.openai.requests.claude.ClaudeResponseData.{ClaudeMessageResponse, ContentBlock}
import sttp.openai.requests.completions.chat.message.Message.UserMessage
import sttp.openai.requests.completions.chat.message.Content.TextContent
import sttp.openai.requests.completions.Usage

object ClaudeFixture {

  val sampleMessageRequest: ClaudeMessageRequest = ClaudeMessageRequest(
    model = ClaudeModel.Claude35Sonnet20241022,
    messages = Seq(UserMessage(TextContent("Hello, Claude!"))),
    maxTokens = 1024,
    temperature = Some(0.7)
  )

  val sampleMessageResponse: ClaudeMessageResponse = ClaudeMessageResponse(
    id = "msg_01EhbVbwKmFHFHnuW5GJwXxx",
    `type` = "message",
    role = "assistant",
    content = Seq(ContentBlock("text", "Hello! How can I assist you today?")),
    model = "claude-3-5-sonnet-20241022",
    stopReason = None,
    stopSequence = None,
    usage = Usage(
      promptTokens = 10,
      completionTokens = 12,
      totalTokens = 22
    )
  )

  val sampleMessageResponseJson: String = """{
    "id": "msg_01EhbVbwKmFHFHnuW5GJwXxx",
    "type": "message",
    "role": "assistant",
    "content": [
      {
        "type": "text",
        "text": "Hello! How can I assist you today?"
      }
    ],
    "model": "claude-3-5-sonnet-20241022",
    "stop_reason": null,
    "stop_sequence": null,
    "usage": {
      "prompt_tokens": 10,
      "completion_tokens": 12,
      "total_tokens": 22
    }
  }"""
}
