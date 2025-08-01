package sttp.openai.requests.responses

import sttp.apispec.Schema
import sttp.openai.json.{SerializationHelpers, SnakePickle}
import sttp.openai.requests.completions.chat.SchemaSupport
import sttp.openai.requests.completions.chat.message.{Tool, ToolChoice}
import ujson.{Obj, Value}

/** @param background
  *   Whether to run the model response in the background. Defaults to false.
  * @param include
  *   Specify additional output data to include in the model response. Currently supported values are:
  *   - code_interpreter_call.outputs: Includes the outputs of python code execution in code interpreter tool call items.
  *   - computer_call_output.output.image_url: Include image urls from the computer call output.
  *   - file_search_call.results: Include the search results of the file search tool call.
  *   - message.input_image.image_url: Include image urls from the input message.
  *   - message.output_text.logprobs: Include logprobs with assistant messages.
  *   - reasoning.encrypted_content: Includes an encrypted version of reasoning tokens in reasoning item outputs.
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
    input: Option[List[String]] = None,
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

  sealed trait Format

  object Format {
    case object Text extends Format

    case object JsonObject extends Format

    case class JsonSchema(name: String, strict: Option[Boolean], schema: Option[Schema], description: Option[String]) extends Format

    implicit private val schemaW: SnakePickle.Writer[Schema] = SchemaSupport.schemaRW

    implicit val jsonSchemaW: SnakePickle.Writer[JsonSchema] = SerializationHelpers.withFlattenedDiscriminator("type", "json_schema")(SnakePickle.macroW)

//    implicit val textRW: SnakePickle.ReadWriter[Text.type] = SnakePickle
//      .readwriter[Value]
//      .bimap[Text.type](
//        _ => Obj("type" -> "text"),
//        _ => Text
//      )
//
//    implicit val jsonObjectRW: SnakePickle.ReadWriter[JsonObject.type] = SnakePickle
//      .readwriter[Value]
//      .bimap[JsonObject.type](
//        _ => Obj("type" -> "json_object"),
//        _ => JsonObject
//      )

    implicit val formatW: SnakePickle.Writer[Format] = SnakePickle
      .writer[Value]
      .comap
        {
          case text: Text.type             => Obj("type" -> "text")
          case jsonObject: JsonObject.type => Obj("type" -> "json_object")
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
