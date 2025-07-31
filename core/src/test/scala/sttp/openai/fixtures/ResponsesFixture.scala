package sttp.openai.fixtures

object ResponsesFixture {
  val jsonRequest: String =
    """{
      |  "background": false,
      |  "include": ["code_interpreter_call.outputs", "message.output_text.logprobs"],
      |  "input": ["What is the capital of France?"],
      |  "instructions": "You are a helpful assistant",
      |  "max_output_tokens": 1000,
      |  "max_tool_calls": 5,
      |  "metadata": {
      |    "key1": "value1",
      |    "key2": "value2"
      |  },
      |  "model": "gpt-4o",
      |  "parallel_tool_calls": true,
      |  "previous_response_id": "prev_resp_123",
      |  "prompt": {
      |    "id": "prompt_123",
      |    "variables": {
      |      "var1": "val1"
      |    },
      |    "version": "1.0"
      |  },
      |  "prompt_cache_key": "cache_key_123",
      |  "reasoning": {
      |    "effort": "high",
      |    "summary": "detailed"
      |  },
      |  "safety_identifier": "safety_123",
      |  "service_tier": "auto",
      |  "store": true,
      |  "stream": false,
      |  "temperature": 0.7,
      |  "text": {
      |    "format": {
      |      "name": "response_schema",
      |      "schema": {"type": "object", "properties": {"answer": {"type": "string"}}},
      |      "description": "Response format",
      |      "strict": "true",
      |      "type": "json_schema"
      |    }
      |  },
      |  "tool_choice": "auto",
      |  "tools": [{"type":"code_interpreter"}],
      |  "top_logprobs": 5,
      |  "top_p": 0.9,
      |  "truncation": "disabled",
      |  "user": "user123"
      |}""".stripMargin
}