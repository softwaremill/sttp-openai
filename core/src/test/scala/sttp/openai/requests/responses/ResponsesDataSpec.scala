package sttp.openai.requests.responses

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.apispec.{Schema, SchemaType}
import sttp.openai.fixtures.ResponsesFixture
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.message.{Tool, ToolChoice}
import sttp.openai.requests.responses.ResponsesRequestBody.Format.JsonSchema
import sttp.openai.requests.responses.ResponsesRequestBody.{
  Format => RequestFormat,
  PromptConfig => RequestPromptConfig,
  ReasoningConfig => RequestReasoningConfig,
  TextConfig => RequestTextConfig,
  _
}
import sttp.openai.requests.responses.ResponsesResponseBody._
import ujson.{Obj, Str}

class ResponsesDataSpec extends AnyFlatSpec with Matchers with EitherValues {

  "Given GetResponseQueryParameters with all fields" should "be properly converted to Map" in {
    // given
    val queryParams = GetResponseQueryParameters(
      include = Some(List("code_interpreter_call.outputs", "message.output_text.logprobs")),
      includeObfuscation = Some(false),
      startingAfter = Some(42),
      stream = Some(true)
    )

    val expectedMap = Map(
      "include" -> "code_interpreter_call.outputs,message.output_text.logprobs",
      "include_obfuscation" -> "false",
      "starting_after" -> "42",
      "stream" -> "true"
    )

    // when
    val resultMap = queryParams.toMap

    // then
    resultMap shouldBe expectedMap
  }

  "Given responses request as case class" should "be properly serialized to Json" in {

    // given
    val givenRequest = ResponsesRequestBody(
      background = Some(false),
      include = Some(List("code_interpreter_call.outputs", "message.output_text.logprobs")),
      input = Some(Left(Input.Text("What is the capital of France?"))),
      instructions = Some("You are a helpful assistant"),
      maxOutputTokens = Some(1000),
      maxToolCalls = Some(5),
      metadata = Some(Map("key1" -> "value1", "key2" -> "value2")),
      model = Some("gpt-4o"),
      parallelToolCalls = Some(true),
      previousResponseId = Some("prev_resp_123"),
      prompt = Some(
        RequestPromptConfig(
          id = "prompt_123",
          variables = Some(Map("var1" -> "val1")),
          version = Some("1.0")
        )
      ),
      promptCacheKey = Some("cache_key_123"),
      reasoning = Some(
        RequestReasoningConfig(
          effort = Some("high"),
          summary = Some("detailed")
        )
      ),
      safetyIdentifier = Some("safety_123"),
      serviceTier = Some("auto"),
      store = Some(true),
      stream = Some(false),
      temperature = Some(0.7),
      text = Some(
        RequestTextConfig(
          format = Some(
            JsonSchema(
              name = "response_schema",
              schema = Some(Schema(SchemaType.String)),
              description = Some("Response format"),
              strict = Some(true)
            )
          )
        )
      ),
      toolChoice = Some(ToolChoice.ToolAuto),
      tools = Some(List(Tool.CodeInterpreterTool)),
      topLogprobs = Some(5),
      topP = Some(0.9),
      truncation = Some("disabled"),
      user = Some("user123")
    )

    val jsonRequest = ujson.read(ResponsesFixture.jsonRequest)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe jsonRequest
  }

  "Given responses request with text format" should "be properly serialized to Json" in {
    import ResponsesRequestBody._

    // given
    val givenRequest = ResponsesRequestBody(
      model = Some("gpt-4o"),
      text = Some(
        RequestTextConfig(
          format = Some(RequestFormat.Text)
        )
      )
    )

    val expectedJson = Obj(
      "model" -> Str("gpt-4o"),
      "text" -> Obj(
        "format" -> Obj(
          "type" -> Str("text")
        )
      )
    )

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe expectedJson
  }

  "Given responses request with input message containing text and image" should "be properly serialized to Json" in {
    import ResponsesRequestBody._
    import Input._
    import InputContentItem._

    // given
    val givenRequest = ResponsesRequestBody(
      model = Some("gpt-4.1"),
      input = Some(
        Right(
          InputMessage(
            content = List(
              InputText("what is in this image?"),
              InputImage(
                detail = "auto", // default detail level
                fileId = None,
                imageUrl = Some(
                  "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
                )
              )
            ),
            role = "user",
            status = None
          ) :: Nil
        )
      )
    )

    val expectedJson = ujson.read(ResponsesFixture.jsonRequestWithInputMessage)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe expectedJson
  }

  "Given responses request with input message containing text and file" should "be properly serialized to Json" in {
    import ResponsesRequestBody._
    import Input._
    import InputContentItem._

    // given
    val givenRequest = ResponsesRequestBody(
      model = Some("gpt-4.1"),
      input = Some(
        Right(
          InputMessage(
            content = List(
              InputText("what is in this file?"),
              InputFile(
                fileData = None,
                fileId = None,
                fileUrl = Some("https://www.berkshirehathaway.com/letters/2024ltr.pdf"),
                filename = None
              )
            ),
            role = "user",
            status = None
          ) :: Nil
        )
      )
    )

    val expectedJson = ujson.read(ResponsesFixture.jsonRequestWithInputFile)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe expectedJson
  }

  "Given responses request with file search tool call" should "be properly serialized to Json" in {
    import ResponsesRequestBody._
    import Input._

    // given
    val givenRequest = ResponsesRequestBody(
      model = Some("gpt-4.1"),
      input = Some(
        Right(
          FileSearchToolCall(
            id = "call_abc123",
            queries = List("machine learning algorithms", "neural networks"),
            status = "completed",
            results = Some(
              List(
                ujson.Obj(
                  "file_id" -> ujson.Str("file-abc123"),
                  "filename" -> ujson.Str("ml_algorithms.pdf"),
                  "content" -> ujson.Str("Neural networks are a subset of machine learning...")
                )
              )
            )
          ) :: Nil
        )
      )
    )

    val expectedJson = ujson.read(ResponsesFixture.jsonRequestWithFileSearchToolCall)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe expectedJson
  }

  "Given responses request with file search tool call in progress" should "be properly serialized to Json" in {
    import ResponsesRequestBody._
    import Input._

    // given
    val givenRequest = ResponsesRequestBody(
      model = Some("gpt-4.1"),
      input = Some(
        Right(
          FileSearchToolCall(
            id = "call_def456",
            queries = List("python programming", "data analysis"),
            status = "in_progress",
            results = None
          ) :: Nil
        )
      )
    )

    val expectedJson = ujson.read(ResponsesFixture.jsonRequestWithFileSearchToolCallInProgress)

    // when
    val serializedJson: ujson.Value = SnakePickle.writeJs(givenRequest)

    // then
    serializedJson shouldBe expectedJson
  }

  "Given responses response with basic output message" should "be properly deserialized from Json" in {

    // given
    val jsonResponse = ujson.read(ResponsesFixture.jsonResponseBasic)

    // when
    val deserializedResponse: ResponsesResponseBody = SnakePickle.read[ResponsesResponseBody](jsonResponse)

    // then
    deserializedResponse.id shouldBe "resp_67ccd3a9da748190baa7f1570fe91ac604becb25c45c1d41"
    deserializedResponse.model shouldBe "gpt-4o-2024-08-06"
    deserializedResponse.`object` shouldBe "response"
    deserializedResponse.status shouldBe "completed"
    deserializedResponse.createdAt shouldBe 1741476777L
    deserializedResponse.parallelToolCalls shouldBe Some(true)
    deserializedResponse.temperature shouldBe Some(1.0)
    deserializedResponse.topP shouldBe Some(1.0)
    deserializedResponse.truncation shouldBe Some("disabled")
    deserializedResponse.metadata shouldBe Some(Map.empty)

    // Check usage structure
    deserializedResponse.usage shouldBe defined
    val usage = deserializedResponse.usage.get
    usage.inputTokens shouldBe 328
    usage.outputTokens shouldBe 52
    usage.totalTokens shouldBe 380
    usage.inputTokensDetails shouldBe Some(InputTokensDetails(cachedTokens = 0))
    usage.outputTokensDetails shouldBe Some(OutputTokensDetails(reasoningTokens = Some(0)))

    // Check the output structure
    deserializedResponse.output should have size 1
    val outputMessage = deserializedResponse.output.head.asInstanceOf[OutputItem.OutputMessage]
    outputMessage.id shouldBe "msg_67ccd3acc8d48190a77525dc6de64b4104becb25c45c1d41"
    outputMessage.role shouldBe "assistant"
    outputMessage.status shouldBe "completed"
    outputMessage.content should have size 1

    val outputText = outputMessage.content.head.asInstanceOf[OutputContent.OutputText]
    outputText.text should include("The image depicts a scenic landscape")
    outputText.annotations shouldBe empty
  }

  "Given responses response with complex output items" should "be properly deserialized from Json" in {

    // given
    val jsonResponse = ujson.read(ResponsesFixture.jsonResponseWithComplexOutput)

    // when
    val deserializedResponse: ResponsesResponseBody = SnakePickle.read[ResponsesResponseBody](jsonResponse)

    // then
    deserializedResponse.id shouldBe "resp_complex123"
    deserializedResponse.model shouldBe "gpt-4o"
    deserializedResponse.`object` shouldBe "response"
    deserializedResponse.status shouldBe "completed"
    deserializedResponse.createdAt shouldBe 1741476778L
    deserializedResponse.instructions shouldBe Some(Left("You are a helpful assistant"))
    deserializedResponse.maxOutputTokens shouldBe Some(2000)
    deserializedResponse.parallelToolCalls shouldBe Some(false)
    deserializedResponse.previousResponseId shouldBe Some("prev_resp_123")
    deserializedResponse.temperature shouldBe Some(0.7)
    deserializedResponse.topP shouldBe Some(0.9)
    deserializedResponse.truncation shouldBe Some("auto")
    deserializedResponse.user shouldBe Some("user123")
    deserializedResponse.metadata shouldBe Some(
      Map(
        "session_id" -> "session_123",
        "experiment" -> "test_run"
      )
    )

    // Check usage structure
    deserializedResponse.usage shouldBe defined
    val usage2 = deserializedResponse.usage.get
    usage2.inputTokens shouldBe 500
    usage2.outputTokens shouldBe 150
    usage2.totalTokens shouldBe 650
    usage2.inputTokensDetails shouldBe Some(InputTokensDetails(cachedTokens = 100))
    usage2.outputTokensDetails shouldBe Some(OutputTokensDetails(reasoningTokens = Some(50)))

    // Check reasoning config
    deserializedResponse.reasoning shouldBe defined
    deserializedResponse.reasoning.get.effort shouldBe Some("medium")
    deserializedResponse.reasoning.get.summary shouldBe Some("concise")

    // Check output structure
    deserializedResponse.output should have size 3

    // Check first output item (message)
    val outputMessage = deserializedResponse.output(0).asInstanceOf[OutputItem.OutputMessage]
    outputMessage.id shouldBe "msg_complex123"
    outputMessage.role shouldBe "assistant"
    outputMessage.status shouldBe "completed"
    outputMessage.content should have size 1

    val outputText = outputMessage.content.head.asInstanceOf[OutputContent.OutputText]
    outputText.text shouldBe "I'll search for information about machine learning."
    outputText.annotations should have size 1

    val citation = outputText.annotations.head.asInstanceOf[OutputContent.Annotation.FileCitation]
    citation.fileId shouldBe "file-123"
    citation.filename shouldBe "ml_guide.pdf"
    citation.index shouldBe 0

    // Check second output item (file search tool call)
    val fileSearchCall = deserializedResponse.output(1).asInstanceOf[OutputItem.FileSearchToolCall]
    fileSearchCall.id shouldBe "call_search123"
    fileSearchCall.queries shouldBe List("machine learning", "neural networks")
    fileSearchCall.status shouldBe "completed"
    fileSearchCall.results shouldBe defined

    // Check third output item (code interpreter tool call)
    val codeCall = deserializedResponse.output(2).asInstanceOf[OutputItem.CodeInterpreterToolCall]
    codeCall.id shouldBe "code_call123"
    codeCall.containerId shouldBe "container_123"
    codeCall.code shouldBe Some("import numpy as np\nprint('Hello ML')")
    codeCall.status shouldBe "completed"
    codeCall.outputs shouldBe defined
  }

}
