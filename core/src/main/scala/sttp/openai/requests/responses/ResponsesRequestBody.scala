package sttp.openai.requests.responses

import sttp.apispec.Schema
import sttp.openai.json.SerializationHelpers.DiscriminatorField
import sttp.openai.json.{SerializationHelpers, SnakePickle}
import sttp.openai.requests.completions.chat.SchemaSupport
import sttp.openai.requests.completions.chat.message.{Tool, ToolChoice}
import sttp.openai.requests.responses.ResponsesRequestBody.Format.{JsonObject, JsonSchema, Text, discriminatorField}
import sttp.openai.requests.responses.ResponsesRequestBody.Input
import sttp.openai.requests.responses.ResponsesRequestBody.Input.InputContentItem.{InputFile, InputImage, InputText}
import ujson.Value

/** @param background
  *   Whether to run the model response in the background. Defaults to false.
  * @param include
  *   Specify additional output data to include in the model response. Currently supported values are:
  *   - `code_interpreter_call.outputs`: Includes the outputs of python code execution in code interpreter tool call items.
  *   - `computer_call_output.output.image_url`: Include image urls from the computer call output.
  *   - `file_search_call.results`: Include the search results of the file search tool call.
  *   - `message.input_image.image_url`: Include image urls from the input message.
  *   - `message.output_text.logprobs`: Include logprobs with assistant messages.
  *   - `reasoning.encrypted_content`: Includes an encrypted version of reasoning tokens in reasoning item outputs.
  * @param input
  *   Text, image, or file inputs to the model, used to generate a response.
  * @param instructions
  *   A system (or developer) message inserted into the model's context.
  * @param maxOutputTokens
  *   An upper bound for the number of tokens that can be generated for a response, including visible output tokens and reasoning tokens.
  * @param maxToolCalls
  *   The maximum number of total calls to built-in tools that can be processed in a response.
  * @param metadata
  *   Set of 16 key-value pairs that can be attached to an object. Keys are strings with a maximum length of 64 characters. Values are
  *   strings with a maximum length of 512 characters.
  * @param model
  *   Model ID used to generate the response, like gpt-4o or o3.
  * @param parallelToolCalls
  *   Whether to allow the model to run tool calls in parallel. Defaults to true.
  * @param previousResponseId
  *   The unique ID of the previous response to the model. Use this to create multi-turn conversations.
  * @param prompt
  *   Reference to a prompt template and its variables.
  * @param promptCacheKey
  *   Used by OpenAI to cache responses for similar requests to optimize your cache hit rates.
  * @param reasoning
  *   Configuration options for reasoning models (o-series models only).
  * @param safetyIdentifier
  *   A stable identifier used to help detect users of your application that may be violating OpenAI's usage policies.
  * @param serviceTier
  *   Specifies the processing type used for serving the request. Defaults to 'auto'.
  * @param store
  *   Whether to store the generated model response for later retrieval via API. Defaults to true.
  * @param stream
  *   If set to true, the model response data will be streamed to the client as it is generated using server-sent events. Defaults to false.
  * @param temperature
  *   What sampling temperature to use, between 0 and 2. Defaults to 1.
  * @param text
  *   Configuration options for a text response from the model. Can be plain text or structured JSON data.
  * @param toolChoice
  *   How the model should select which tool (or tools) to use when generating a response.
  * @param tools
  *   An array of tools the model may call while generating a response.
  * @param topLogprobs
  *   An integer between 0 and 20 specifying the number of most likely tokens to return at each token position.
  * @param topP
  *   An alternative to sampling with temperature, called nucleus sampling. Defaults to 1.
  * @param truncation
  *   The truncation strategy to use for the model response. Defaults to 'disabled'.
  * @param user
  *   Deprecated. Use safetyIdentifier and promptCacheKey instead.
  */
case class ResponsesRequestBody(
    background: Option[Boolean] = None,
    include: Option[List[String]] = None,
    input: Option[Either[Input.Text,List[Input]]] = None,
    instructions: Option[String] = None,
    maxOutputTokens: Option[Int] = None,
    maxToolCalls: Option[Int] = None,
    metadata: Option[Map[String, String]] = None,
    model: Option[String] = None,
    parallelToolCalls: Option[Boolean] = None,
    previousResponseId: Option[String] = None,
    prompt: Option[ResponsesRequestBody.PromptConfig] = None,
    promptCacheKey: Option[String] = None,
    reasoning: Option[ResponsesRequestBody.ReasoningConfig] = None,
    safetyIdentifier: Option[String] = None,
    serviceTier: Option[String] = None,
    store: Option[Boolean] = None,
    stream: Option[Boolean] = None,
    temperature: Option[Double] = None,
    text: Option[ResponsesRequestBody.TextConfig] = None,
    toolChoice: Option[ToolChoice] = None,
    tools: Option[List[Tool]] = None,
    topLogprobs: Option[Int] = None,
    topP: Option[Double] = None,
    truncation: Option[String] = None,
    user: Option[String] = None
)

object ResponsesRequestBody {

  case class PromptConfig(
      id: String,
      variables: Option[Map[String, String]] = None,
      version: Option[String] = None
  )

  case class ReasoningConfig(
      effort: Option[String] = None,
      summary: Option[String] = None
  )

  sealed trait Input
  object Input {
    private val discriminatorField = DiscriminatorField("type")

    sealed trait InputContentItem
    object InputContentItem {
      case class InputText(text: String) extends InputContentItem
      case class InputImage(detail: String, fileId: Option[String], imageUrl: Option[String]) extends InputContentItem
      case class InputFile(fileData: Option[String], fileId: Option[String], fileUrl: Option[String], filename: Option[String])
          extends InputContentItem
    }

    implicit val inputTextW: SnakePickle.Writer[InputText] =
      SerializationHelpers.withFlattenedDiscriminator(discriminatorField, "input_text")(SnakePickle.macroW)

    implicit val inputImageW: SnakePickle.Writer[InputImage] =
      SerializationHelpers.withFlattenedDiscriminator(discriminatorField, "input_image")(SnakePickle.macroW)

    implicit val inputFileW: SnakePickle.Writer[InputFile] =
      SerializationHelpers.withFlattenedDiscriminator(discriminatorField, "input_file")(SnakePickle.macroW)

    implicit val inputContentItemW: SnakePickle.Writer[InputContentItem] = SnakePickle.writer[Value].comap {
      case inputText: InputText => SnakePickle.writeJs(inputText)
      case inputImage: InputImage => SnakePickle.writeJs(inputImage)
      case inputFile: InputFile => SnakePickle.writeJs(inputFile)
    }
    sealed trait OutputContentItem
    object OutputContentItem {

      sealed trait Annotation
      object Annotation {
        case class FileCitation(
            fileId: String,
            filename: String,
            index: Int
        ) extends Annotation

        case class UrlCitation(
            endIndex: Int,
            startIndex: Int,
            title: String,
            url: String
        ) extends Annotation

        case class ContainerFileCitation(
            containerId: String,
            endIndex: Int,
            fileId: String,
            filename: String,
            startIndex: Int
        ) extends Annotation

        case class FilePath(
            fileId: String,
            index: Int
        ) extends Annotation
      }

      case class LogProb(
          bytes: List[Byte],
          logprob: Double,
          token: String,
          topLogprobs: List[TopLogProb]
      )

      case class TopLogProb(
          bytes: List[Byte],
          logprob: Double,
          token: String
      )

      case class OutputText(
          annotations: List[Annotation],
          text: String,
          logprobs: Option[List[LogProb]] = None
      ) extends OutputContentItem

    }

    case class Text(text: String)
    case class InputMessage(content: List[InputContentItem], role: String, status: Option[String]) extends Input
    case class OutputMessage(content: OutputContentItem, id: String, role: String, status: String) extends Input
    case class ItemReference(id: String) extends Input

    implicit val inputMessageW: SnakePickle.Writer[InputMessage] =
      SerializationHelpers.withFlattenedDiscriminator(discriminatorField, "message")(SnakePickle.macroW)

    implicit val textOrInputListW: SnakePickle.Writer[Either[Input.Text, List[Input]]] = SnakePickle.writer[Value].comap {
      case Left(value) => SnakePickle.writeJs(value)
      case Right(value) => SnakePickle.writeJs(value)
    }

    implicit val textW: SnakePickle.Writer[Text] = SnakePickle.writer[Value].comap(t => ujson.Str(t.text))

    implicit val inputW: SnakePickle.Writer[Input] = SnakePickle.writer[Value].comap {
      case inputMessage: InputMessage => SnakePickle.writeJs(inputMessage)
      case OutputMessage(content, id, role, status) => ???
      case ItemReference(id) => ???
      case FileSearchToolCall(id, queries, status, results) => ???
      case ComputerToolCall(action, callId, id, pendingSafetyChecks) => ???
      case ComputerToolCallOutput(callId, output, acknowledgedSafetyChecks, id, status) => ???
      case WebSearchToolCall(action, id, status) => ???
      case FunctionToolCall(arguments, callId, name, id, status) => ???
      case FunctionToolCallOutput(callId, output, id, status) => ???
      case Reasoning(id, summary, encryptedContent, status) => ???
      case ImageGenerationCall(id, result, status) => ???
      case CodeInterpreterToolCall(code, containerId, id, outputs, status) => ???
      case LocalShellCall(action, callId, id, status) => ???
      case LocalShellCallOutput(id, output, status) => ???
      case McpListTools(id, serverLabel, tools, error) => ???
      case McpApprovalRequest(arguments, id, name, serverLabel) => ???
      case McpApprovalResponse(approvalRequestId, approve, id, reason) => ???
      case McpToolCall(arguments, id, name, serverLabel, error, output) => ???

    }

    // File search tool call
    case class FileSearchToolCall(
        id: String,
        queries: List[String],
        status: String,
        results: Option[List[Value]] = None
    ) extends Input

    // Computer tool call
    case class ComputerToolCall(
        action: Value,
        callId: String,
        id: String,
        pendingSafetyChecks: List[ComputerToolCall.PendingSafetyCheck]
    ) extends Input

    object ComputerToolCall {
      case class PendingSafetyCheck(
          code: String,
          id: String,
          message: String,
          status: String
      )
    }

    // Computer tool call output
    case class ComputerToolCallOutput(
        callId: String,
        output: ComputerToolCallOutput.ComputerScreenshot,
        acknowledgedSafetyChecks: Option[List[Value]] = None,
        id: Option[String] = None,
        status: Option[String] = None
    ) extends Input

    object ComputerToolCallOutput {
      case class ComputerScreenshot(
          fileId: Option[String] = None,
          imageUrl: Option[String] = None
      )
    }

    // Web search tool call
    case class WebSearchToolCall(
        action: Value,
        id: String,
        status: String
    ) extends Input

    // Function tool call
    case class FunctionToolCall(
        arguments: String,
        callId: String,
        name: String,
        id: Option[String] = None,
        status: Option[String] = None
    ) extends Input

    // Function tool call output
    case class FunctionToolCallOutput(
        callId: String,
        output: String,
        id: Option[String] = None,
        status: Option[String] = None
    ) extends Input

    // Reasoning
    case class Reasoning(
        id: String,
        summary: List[Reasoning.SummaryText],
        encryptedContent: Option[String] = None,
        status: Option[String] = None
    ) extends Input

    object Reasoning {
      case class SummaryText(
          text: String
      )
    }

    // Image generation call
    case class ImageGenerationCall(
        id: String,
        result: Option[String],
        status: String
    ) extends Input

    // Code interpreter tool call
    case class CodeInterpreterToolCall(
        code: Option[String],
        containerId: String,
        id: String,
        outputs: Option[List[Value]] = None,
        status: String
    ) extends Input

    // Local shell call
    case class LocalShellCall(
        action: Value,
        callId: String,
        id: String,
        status: String
    ) extends Input

    // Local shell call output
    case class LocalShellCallOutput(
        id: String,
        output: String,
        status: Option[String] = None
    ) extends Input

    // MCP list tools
    case class McpListTools(
        id: String,
        serverLabel: String,
        tools: List[Value],
        error: Option[String] = None
    ) extends Input

    // MCP approval request
    case class McpApprovalRequest(
        arguments: String,
        id: String,
        name: String,
        serverLabel: String
    ) extends Input

    // MCP approval response
    case class McpApprovalResponse(
        approvalRequestId: String,
        approve: Boolean,
        id: Option[String] = None,
        reason: Option[String] = None
    ) extends Input

    // MCP tool call
    case class McpToolCall(
        arguments: String,
        id: String,
        name: String,
        serverLabel: String,
        error: Option[String] = None,
        output: Option[String] = None
    ) extends Input

  }

  sealed trait Format

  object Format {
    case object Text extends Format

    case object JsonObject extends Format

    case class JsonSchema(name: String, strict: Option[Boolean], schema: Option[Schema], description: Option[String]) extends Format

    implicit private val schemaW: SnakePickle.Writer[Schema] = SchemaSupport.schemaRW

    private val discriminatorField = DiscriminatorField("type")
    implicit val jsonSchemaW: SnakePickle.Writer[JsonSchema] =
      SerializationHelpers.withFlattenedDiscriminator(discriminatorField, "json_schema")(SnakePickle.macroW)

    implicit val textW: SnakePickle.Writer[Text.type] = SerializationHelpers.caseObjectWithDiscriminatorWriter(discriminatorField, "text")

    implicit val jsonObjectW: SnakePickle.Writer[JsonObject.type] =
      SerializationHelpers.caseObjectWithDiscriminatorWriter(discriminatorField, "json_object")

    implicit val formatW: SnakePickle.Writer[Format] = SnakePickle.writer[Value].comap {
      case text: Text.type             => SnakePickle.writeJs(text)
      case jsonObject: JsonObject.type => SnakePickle.writeJs(jsonObject)
      case jsonSchema: JsonSchema      => SnakePickle.writeJs(jsonSchema)
    }
  }
  case class TextConfig(
      format: Option[Format] = None
  )

  implicit val promptConfigRW: SnakePickle.ReadWriter[PromptConfig] = SnakePickle.macroRW
  implicit val reasoningConfigRW: SnakePickle.ReadWriter[ReasoningConfig] = SnakePickle.macroRW
  implicit val textConfigRW: SnakePickle.Writer[TextConfig] = SnakePickle.macroW
  implicit val responsesRequestBodyW: SnakePickle.Writer[ResponsesRequestBody] = SnakePickle.macroW
}
