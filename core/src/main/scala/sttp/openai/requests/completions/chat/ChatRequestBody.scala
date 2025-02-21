package sttp.openai.requests.completions.chat

import io.circe.syntax._
import io.circe.{DecodingFailure, Json, JsonNumber, JsonObject}
import sttp.apispec.Schema
import sttp.apispec.circe._
import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.Stop
import sttp.openai.requests.completions.chat.message.{Message, Tool, ToolChoice}
import ujson._
import ujson.circe.CirceJson

object ChatRequestBody {

  sealed trait ResponseFormat

  object ResponseFormat {
    case object Text extends ResponseFormat
    case object JsonObject extends ResponseFormat
    case class JsonSchema(name: String, strict: Boolean, schema: Schema) extends ResponseFormat
    object JsonSchema {
      case class ParseException(circeException: DecodingFailure) extends Exception("Failed to parse JSON schema", circeException)

      private case class InternalRepr(json_schema: JsonSchema)

      private object InternalRepr {
        case class FolderState(
            fields: List[(String, Json)],
            addAdditionalProperties: Boolean,
            requiredProperties: List[String]
        )

        /** OpenAI's JSON schema support imposes two requirements:
          *
          *   1. All fields must be `required`: https://platform.openai.com/docs/guides/structured-outputs/all-fields-must-be-required
          *
          * 2. `additionalProperties: false` must always be set in objects:
          * https://platform.openai.com/docs/guides/structured-outputs/additionalproperties-false-must-always-be-set-in-objects
          *
          * We implement these by folding over the JSON structure. However, if a schema uses discriminated unions (indicated by a
          * `discriminator` property), we skip forcing `additionalProperties: false` to preserve flexibility in selecting sub-schemas.
          */
        private val schemaFolder: Json.Folder[Json] = new Json.Folder[Json] {
          lazy val onNull = Json.Null
          def onBoolean(value: Boolean): Json = Json.fromBoolean(value)
          def onNumber(value: JsonNumber): Json = Json.fromJsonNumber(value)
          def onString(value: String): Json = Json.fromString(value)
          def onArray(value: Vector[Json]): Json = Json.fromValues(value.map(_.foldWith(this)))
          def onObject(value: JsonObject): Json = {
            val state = value.toList.foldRight(FolderState(Nil, false, Nil)) { case ((k, v), acc) =>
              if (k == "properties")
                acc.copy(
                  fields = (k, v.foldWith(this)) :: acc.fields,
                  addAdditionalProperties = true,
                  requiredProperties = v.asObject.fold(List.empty[String])(_.keys.toList)
                )
              else if (k == "type")
                acc.copy(
                  fields = (k, v.foldWith(this)) :: acc.fields,
                  addAdditionalProperties = acc.addAdditionalProperties || v.asString.contains("object")
                )
              else
                acc.copy(fields = (k, v.foldWith(this)) :: acc.fields)
            }

            // Detect if this object is part of a discriminated union by checking for a "discriminator" property.
            val isDiscriminatedUnion = value.contains("discriminator")

            val (addlPropsRemove, addlPropsAdd) =
              if (state.addAdditionalProperties && !isDiscriminatedUnion)
                (Set("additionalProperties"), List("additionalProperties" := false))
              else
                (Set(), Nil)

            val (requiredRemove, requiredAdd) =
              if (state.requiredProperties.nonEmpty)
                (Set("required"), List("required" := state.requiredProperties))
              else
                (Set(), Nil)

            val remove = addlPropsRemove ++ requiredRemove
            val fields = addlPropsAdd ++ requiredAdd ++ state.fields.filterNot { case (k, _) => remove.contains(k) }

            Json.fromFields(fields)
          }
        }

        implicit private val schemaRW: SnakePickle.ReadWriter[Schema] = SnakePickle
          .readwriter[Value]
          .bimap(
            s => CirceJson.transform(s.asJson.deepDropNullValues.foldWith(schemaFolder), upickle.default.reader[Value]),
            v =>
              upickle.default.transform(v).to(CirceJson).as[Schema] match {
                case Left(e)  => throw new ParseException(e)
                case Right(s) => s
              }
          )

        implicit private val jsonSchemaRW: SnakePickle.ReadWriter[JsonSchema] = SnakePickle
          .readwriter[Value]
          .bimap(
            s => Obj("name" -> s.name, "strict" -> s.strict, "schema" -> SnakePickle.writeJs(s.schema)),
            v => {
              val o = v.obj
              JsonSchema(
                name = o("name").str,
                strict = o("strict").bool,
                schema = SnakePickle.read[Schema](o("schema"))
              )
            }
          )

        implicit val internalReprRW: SnakePickle.ReadWriter[InternalRepr] = SnakePickle.macroRW
      }

      implicit val jsonSchemaRW: SnakePickle.ReadWriter[JsonSchema] = SnakePickle
        .readwriter[Value]
        .bimap[JsonSchema](
          s => Obj(SnakePickle.writeJs(InternalRepr(s)).obj.addOne("type" -> "json_schema")),
          v => SnakePickle.read[InternalRepr](v).json_schema
        )
    }

    implicit val textRW: SnakePickle.ReadWriter[Text.type] = SnakePickle
      .readwriter[Value]
      .bimap[Text.type](
        _ => Obj("type" -> "text"),
        _ => Text
      )

    implicit val jsonObjectRW: SnakePickle.ReadWriter[JsonObject.type] = SnakePickle
      .readwriter[Value]
      .bimap[JsonObject.type](
        _ => Obj("type" -> "json_object"),
        _ => JsonObject
      )

    implicit val responseFormatRW: SnakePickle.ReadWriter[ResponseFormat] = SnakePickle
      .readwriter[Value]
      .bimap[ResponseFormat](
        {
          case text: Text.type             => SnakePickle.writeJs(text)
          case jsonObject: JsonObject.type => SnakePickle.writeJs(jsonObject)
          case jsonSchema: JsonSchema      => SnakePickle.writeJs(jsonSchema)
        },
        json =>
          json("type").str match {
            case "text"        => SnakePickle.read[Text.type](json)
            case "json_object" => SnakePickle.read[JsonObject.type](json)
            case "json_schema" => SnakePickle.read[JsonSchema](json)
          }
      )
  }

  /** @param messages
    *   A list of messages describing the conversation so far.
    * @param model
    *   ID of the model to use.
    * @param frequencyPenalty
    *   Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing
    *   the model's likelihood to repeat the same line verbatim.
    * @param logitBias
    *   Modify the likelihood of specified tokens appearing in the completion.
    * @param maxTokens
    *   The maximum number of tokens to generate in the chat completion.
    * @param n
    *   How many chat completion choices to generate for each input message.
    * @param presencePenalty
    *   Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the
    *   model's likelihood to talk about new topics.
    * @param responseFormat
    *   An object specifying the format that the model must output. Setting to {"type": "json_object"} enables JSON mode, which guarantees
    *   the message the model generates is valid JSON.
    * @param seed
    *   This feature is in Beta. If specified, our system will make a best effort to sample deterministically, such that repeated requests
    *   with the same seed and parameters should return the same result. Determinism is not guaranteed, and you should refer to the
    *   system_fingerprint response parameter to monitor changes in the backend.
    * @param stop
    *   Up to 4 sequences where the API will stop generating further tokens.
    * @param temperature
    *   What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like
    *   0.2 will make it more focused and deterministic.
    * @param topP
    *   An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p
    *   probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
    * @param tools
    *   A list of tools the model may call. Currently, only functions are supported as a tool. Use this to provide a list of functions the
    *   model may generate JSON inputs for.
    * @param toolChoice
    *   Controls which (if any) function is called by the model.
    * @param user
    *   A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
    */
  case class ChatBody(
      messages: Seq[Message],
      model: ChatCompletionModel,
      frequencyPenalty: Option[Double] = None,
      logitBias: Option[Map[String, Float]] = None,
      maxTokens: Option[Int] = None,
      n: Option[Int] = None,
      presencePenalty: Option[Double] = None,
      responseFormat: Option[ResponseFormat] = None,
      seed: Option[Int] = None,
      stop: Option[Stop] = None,
      temperature: Option[Double] = None,
      topP: Option[Double] = None,
      tools: Option[Seq[Tool]] = None,
      toolChoice: Option[ToolChoice] = None,
      user: Option[String] = None
  )

  object ChatBody {
    def withStreaming(chatBody: ChatBody): ujson.Value = {
      val json = SnakePickle.writeJs(chatBody)
      json.obj("stream") = true
      json
    }

    implicit val chatRequestW: SnakePickle.Writer[ChatBody] = SnakePickle.macroW[ChatBody]
  }

  sealed abstract class ChatCompletionModel(val value: String)

  object ChatCompletionModel {
    implicit val chatCompletionModelRW: SnakePickle.ReadWriter[ChatCompletionModel] = SnakePickle
      .readwriter[ujson.Value]
      .bimap[ChatCompletionModel](
        model => SnakePickle.writeJs(model.value),
        jsonValue =>
          SnakePickle.read[ujson.Value](jsonValue) match {
            case Str(value) =>
              byChatModelValue.getOrElse(value, CustomChatCompletionModel(value))
            case e => throw DeserializationOpenAIException(new Exception(s"Could not deserialize: $e"))
          }
      )

    case object GPT4 extends ChatCompletionModel("gpt-4")

    case object GPT40314 extends ChatCompletionModel("gpt-4-0314")

    case object GPT432k extends ChatCompletionModel("gpt-4-32k")

    case object GPT432k0314 extends ChatCompletionModel("gpt-4-32k-0314")

    case object GPT35Turbo extends ChatCompletionModel("gpt-3.5-turbo")

    case object GPT35Turbo0301 extends ChatCompletionModel("gpt-3.5-turbo-0301")

    case object GPT4Turbo extends ChatCompletionModel("gpt-4-1106-preview")

    case object GPT4o extends ChatCompletionModel("gpt-4o")

    case object GPT4oMini extends ChatCompletionModel("gpt-4o-mini")

    case class CustomChatCompletionModel(customChatCompletionModel: String) extends ChatCompletionModel(customChatCompletionModel)

    val values: Set[ChatCompletionModel] =
      Set(
        GPT4,
        GPT40314,
        GPT432k,
        GPT432k0314,
        GPT35Turbo,
        GPT35Turbo0301,
        GPT4Turbo,
        GPT4o,
        GPT4oMini
      )

    private val byChatModelValue = values.map(model => model.value -> model).toMap
  }

}
