package sttp.openai.requests.responses

import sttp.apispec.Schema
import sttp.openai.json.{SerializationHelpers, SnakePickle}
import sttp.openai.requests.completions.chat.SchemaSupport
import sttp.openai.requests.completions.chat.message.{Tool, ToolChoice}
import sttp.openai.requests.responses.ResponsesRequestBody.Input
import sttp.openai.requests.responses.ResponsesRequestBody.Input.OutputContentItem.OutputText.{Annotation, LogProb}
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
    input: Option[Either[Input.Text, List[Input]]] = None,
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
    sealed trait InputContentItem
    object InputContentItem {
      @upickle.implicits.key("input_text")
      case class InputText(text: String) extends InputContentItem

      @upickle.implicits.key("input_image")
      case class InputImage(detail: String, fileId: Option[String], imageUrl: Option[String]) extends InputContentItem

      @upickle.implicits.key("input_file")
      case class InputFile(fileData: Option[String], fileId: Option[String], fileUrl: Option[String], filename: Option[String])
          extends InputContentItem

      implicit val inputTextW: SnakePickle.Writer[InputText] = SnakePickle.macroW

      implicit val inputImageW: SnakePickle.Writer[InputImage] = SnakePickle.macroW

      implicit val inputFileW: SnakePickle.Writer[InputFile] = SnakePickle.macroW

      implicit val inputContentItemW: SnakePickle.Writer[InputContentItem] = SnakePickle.writer[Value].comap {
        case inputText: InputText   => SnakePickle.writeJs(inputText)
        case inputImage: InputImage => SnakePickle.writeJs(inputImage)
        case inputFile: InputFile   => SnakePickle.writeJs(inputFile)
      }
    }

    sealed trait OutputContentItem

    object OutputContentItem {
      object OutputText {
        sealed trait Annotation

        object Annotation {
          @upickle.implicits.key("file_citation")
          case class FileCitation(fileId: String, filename: String, index: Int) extends Annotation

          @upickle.implicits.key("url_citation")
          case class UrlCitation(endIndex: Int, startIndex: Int, title: String, url: String) extends Annotation

          @upickle.implicits.key("container_file_citation")
          case class ContainerFileCitation(containerId: String, endIndex: Int, fileId: String, filename: String, startIndex: Int)
              extends Annotation

          @upickle.implicits.key("file_path")
          case class FilePath(fileId: String, index: Int) extends Annotation

          implicit val fileCitationW: SnakePickle.Writer[FileCitation] = SnakePickle.macroW

          implicit val urlCitationW: SnakePickle.Writer[UrlCitation] = SnakePickle.macroW

          implicit val containerFileCitationW: SnakePickle.Writer[ContainerFileCitation] = SnakePickle.macroW

          implicit val filePathW: SnakePickle.Writer[FilePath] = SnakePickle.macroW

          implicit val annotationW: SnakePickle.Writer[Annotation] = SnakePickle.writer[Value].comap {
            case fileCitation: FileCitation                   => SnakePickle.writeJs(fileCitation)
            case urlCitation: UrlCitation                     => SnakePickle.writeJs(urlCitation)
            case containerFileCitation: ContainerFileCitation => SnakePickle.writeJs(containerFileCitation)
            case filePath: FilePath                           => SnakePickle.writeJs(filePath)
          }
        }

        case class TopLogProb(bytes: List[Byte], logprob: Double, token: String)

        case class LogProb(bytes: List[Byte], logprob: Double, token: String, topLogprobs: List[TopLogProb])

        implicit val topLogProbW: SnakePickle.Writer[TopLogProb] = SnakePickle.macroW

        implicit val logProbW: SnakePickle.Writer[LogProb] = SnakePickle.macroW
      }

      @upickle.implicits.key("output_text")
      case class OutputText(annotations: List[Annotation], text: String, logprobs: Option[List[LogProb]] = None) extends OutputContentItem

      @upickle.implicits.key("refusal")
      case class Refusal(refusal: String) extends OutputContentItem

      implicit val outputTextW: SnakePickle.Writer[OutputText] = SnakePickle.macroW

      implicit val refusalW: SnakePickle.Writer[Refusal] = SnakePickle.macroW

      implicit val outputContentItemW: SnakePickle.Writer[OutputContentItem] = SnakePickle.writer[Value].comap {
        case outputText: OutputText => SnakePickle.writeJs(outputText)
        case refusal: Refusal       => SnakePickle.writeJs(refusal)
      }
    }

    case class Text(text: String)

    @upickle.implicits.key("message")
    case class InputMessage(content: List[InputContentItem], role: String, status: Option[String]) extends Input

    @upickle.implicits.key("message")
    case class OutputMessage(content: List[OutputContentItem], id: String, role: String, status: String) extends Input

    @upickle.implicits.key("file_search_call")
    case class FileSearchToolCall(id: String, queries: List[String], status: String, results: Option[List[Value]] = None) extends Input

    object ComputerToolCall {
      case class PendingSafetyCheck(code: String, id: String, message: String, status: String)

      implicit val pendingSafetyCheckW: SnakePickle.Writer[PendingSafetyCheck] = SnakePickle.macroW
    }

    @upickle.implicits.key("computer_call")
    case class ComputerToolCall(action: Value, callId: String, id: String, pendingSafetyChecks: List[ComputerToolCall.PendingSafetyCheck])
        extends Input

    object ComputerToolCallOutput {
      case class ComputerScreenshot(fileId: Option[String] = None, imageUrl: Option[String] = None)

      implicit val computerScreenshotW: SnakePickle.Writer[ComputerScreenshot] = SnakePickle.macroW
    }

    @upickle.implicits.key("computer_call_output")
    case class ComputerToolCallOutput(
        callId: String,
        output: ComputerToolCallOutput.ComputerScreenshot,
        acknowledgedSafetyChecks: Option[List[Value]] = None,
        id: Option[String] = None,
        status: Option[String] = None
    ) extends Input

    @upickle.implicits.key("web_search_call")
    case class WebSearchToolCall(action: Value, id: String, status: String) extends Input

    @upickle.implicits.key("function_call")
    case class FunctionToolCall(arguments: String, callId: String, name: String, id: Option[String] = None, status: Option[String] = None)
        extends Input

    @upickle.implicits.key("function_call_output")
    case class FunctionToolCallOutput(callId: String, output: String, id: Option[String] = None, status: Option[String] = None)
        extends Input

    object Reasoning {
      case class SummaryText(text: String)

      implicit val summaryTextW: SnakePickle.Writer[SummaryText] = SnakePickle.macroW
    }

    @upickle.implicits.key("reasoning")
    case class Reasoning(
        id: String,
        summary: List[Reasoning.SummaryText],
        encryptedContent: Option[String] = None,
        status: Option[String] = None
    ) extends Input

    @upickle.implicits.key("image_generation_call")
    case class ImageGenerationCall(id: String, result: Option[String], status: String) extends Input

    @upickle.implicits.key("code_interpreter_call")
    case class CodeInterpreterToolCall(
        code: Option[String],
        containerId: String,
        id: String,
        outputs: Option[List[Value]] = None,
        status: String
    ) extends Input

    @upickle.implicits.key("local_shell_call")
    case class LocalShellCall(action: Value, callId: String, id: String, status: String) extends Input

    @upickle.implicits.key("local_shell_call_output")
    case class LocalShellCallOutput(id: String, output: String, status: Option[String] = None) extends Input

    @upickle.implicits.key("mcp_list_tools")
    case class McpListTools(id: String, serverLabel: String, tools: List[Value], error: Option[String] = None) extends Input

    @upickle.implicits.key("mcp_approval_request")
    case class McpApprovalRequest(arguments: String, id: String, name: String, serverLabel: String) extends Input

    @upickle.implicits.key("mcp_approval_response")
    case class McpApprovalResponse(approvalRequestId: String, approve: Boolean, id: Option[String] = None, reason: Option[String] = None)
        extends Input

    @upickle.implicits.key("mcp_tool_call")
    case class McpToolCall(
        arguments: String,
        id: String,
        name: String,
        serverLabel: String,
        error: Option[String] = None,
        output: Option[String] = None
    ) extends Input

    @upickle.implicits.key("item_reference")
    case class ItemReference(id: String) extends Input

    implicit val textW: SnakePickle.Writer[Text] = SnakePickle.writer[Value].comap(t => ujson.Str(t.text))

    implicit val textOrInputListW: SnakePickle.Writer[Either[Input.Text, List[Input]]] = SnakePickle.writer[Value].comap {
      case Left(value)  => SnakePickle.writeJs(value)
      case Right(value) => SnakePickle.writeJs(value)
    }

    implicit val inputMessageW: SnakePickle.Writer[InputMessage] = SnakePickle.macroW

    implicit val outputMessageW: SnakePickle.Writer[OutputMessage] = SnakePickle.macroW

    implicit val fileSearchToolCallW: SnakePickle.Writer[FileSearchToolCall] = SnakePickle.macroW

    implicit val computerToolCallW: SnakePickle.Writer[ComputerToolCall] = SnakePickle.macroW

    implicit val computerToolCallOutputW: SnakePickle.Writer[ComputerToolCallOutput] = SnakePickle.macroW

    implicit val webSearchToolCallW: SnakePickle.Writer[WebSearchToolCall] = SnakePickle.macroW

    implicit val functionToolCallW: SnakePickle.Writer[FunctionToolCall] = SnakePickle.macroW

    implicit val functionToolCallOutputW: SnakePickle.Writer[FunctionToolCallOutput] = SnakePickle.macroW

    implicit val reasoningW: SnakePickle.Writer[Reasoning] = SnakePickle.macroW

    implicit val imageGenerationCallW: SnakePickle.Writer[ImageGenerationCall] = SnakePickle.macroW

    implicit val codeInterpreterToolCallW: SnakePickle.Writer[CodeInterpreterToolCall] = SnakePickle.macroW

    implicit val localShellCallW: SnakePickle.Writer[LocalShellCall] = SnakePickle.macroW

    implicit val localShellCallOutputW: SnakePickle.Writer[LocalShellCallOutput] = SnakePickle.macroW

    implicit val mcpListToolsW: SnakePickle.Writer[McpListTools] = SnakePickle.macroW

    implicit val mcpApprovalRequestW: SnakePickle.Writer[McpApprovalRequest] = SnakePickle.macroW

    implicit val mcpApprovalResponseW: SnakePickle.Writer[McpApprovalResponse] = SnakePickle.macroW

    implicit val mcpToolCallW: SnakePickle.Writer[McpToolCall] = SnakePickle.macroW

    implicit val itemReferenceW: SnakePickle.Writer[ItemReference] = SnakePickle.macroW

    implicit val inputW: SnakePickle.Writer[Input] = SnakePickle.writer[Value].comap {
      case inputMessage: InputMessage                       => SnakePickle.writeJs(inputMessage)
      case outputMessage: OutputMessage                     => SnakePickle.writeJs(outputMessage)
      case fileSearchToolCall: FileSearchToolCall           => SnakePickle.writeJs(fileSearchToolCall)
      case computerToolCall: ComputerToolCall               => SnakePickle.writeJs(computerToolCall)
      case computerToolCallOutput: ComputerToolCallOutput   => SnakePickle.writeJs(computerToolCallOutput)
      case webSearchToolCall: WebSearchToolCall             => SnakePickle.writeJs(webSearchToolCall)
      case functionToolCall: FunctionToolCall               => SnakePickle.writeJs(functionToolCall)
      case functionToolCallOutput: FunctionToolCallOutput   => SnakePickle.writeJs(functionToolCallOutput)
      case reasoning: Reasoning                             => SnakePickle.writeJs(reasoning)
      case imageGenerationCall: ImageGenerationCall         => SnakePickle.writeJs(imageGenerationCall)
      case codeInterpreterToolCall: CodeInterpreterToolCall => SnakePickle.writeJs(codeInterpreterToolCall)
      case localShellCall: LocalShellCall                   => SnakePickle.writeJs(localShellCall)
      case localShellCallOutput: LocalShellCallOutput       => SnakePickle.writeJs(localShellCallOutput)
      case mcpListTools: McpListTools                       => SnakePickle.writeJs(mcpListTools)
      case mcpApprovalRequest: McpApprovalRequest           => SnakePickle.writeJs(mcpApprovalRequest)
      case mcpApprovalResponse: McpApprovalResponse         => SnakePickle.writeJs(mcpApprovalResponse)
      case mcpToolCall: McpToolCall                         => SnakePickle.writeJs(mcpToolCall)
      case itemReference: ItemReference                     => SnakePickle.writeJs(itemReference)
    }

  }

  sealed trait Format

  object Format {
    case object Text extends Format

    case object JsonObject extends Format

    @upickle.implicits.key("json_schema")
    case class JsonSchema(name: String, strict: Option[Boolean], schema: Option[Schema], description: Option[String]) extends Format

    implicit private val schemaW: SnakePickle.Writer[Schema] = SchemaSupport.schemaRW

    implicit val jsonSchemaW: SnakePickle.Writer[JsonSchema] = SnakePickle.macroW

    implicit val textW: SnakePickle.Writer[Text.type] = SerializationHelpers.caseObjectWithDiscriminatorWriter("text")

    implicit val jsonObjectW: SnakePickle.Writer[JsonObject.type] = SerializationHelpers.caseObjectWithDiscriminatorWriter("json_object")

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
