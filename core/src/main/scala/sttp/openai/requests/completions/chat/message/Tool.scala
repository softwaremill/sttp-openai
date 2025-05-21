package sttp.openai.requests.completions.chat.message

import sttp.apispec.Schema
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.SchemaSupport
import sttp.tapir.{Schema => TSchema}
import sttp.tapir.docs.apispec.schema.TapirSchemaToJsonSchema
import ujson._

sealed trait Tool

object Tool {

  /** Function tool definition – supports structured outputs by allowing the optional `strict` flag.
    *
    * @param description
    *   A description of what the function does, used by the model to choose when and how to call the function.
    * @param name
    *   The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
    * @param parameters
    *   The parameters the functions accepts, described as a JSON Schema object.
    * @param strict
    *   When set to `true`, [Structured Outputs](https://platform.openai.com/docs/guides/structured-outputs) validation will be enforced by
    *   the OpenAI API. Defaults to `None`, meaning the field is omitted in the outgoing JSON.
    */
  case class FunctionTool(
      description: String,
      name: String,
      parameters: Map[String, Value],
      strict: Option[Boolean] = None
  ) extends Tool

  /** With this class we are not forced to provide Json Schema by hand. It will be automatically generated based on the type `T`. Please
    * refer to readme for example of usage.
    *
    * @param description
    *   A description of what the function does.
    * @param name
    *   The name of the function to be called.
    * @param parameters
    *   The Tapir derived [[sttp.apispec.Schema]] for the function parameters.
    * @param strict
    *   Optional strict flag (see [[FunctionTool.strict]]).
    */
  case class SchematizedFunctionTool(
      description: String,
      name: String,
      parameters: Schema,
      strict: Option[Boolean] = None
  ) extends Tool

  object SchematizedFunctionTool {
    def apply[T: TSchema](description: String, name: String): SchematizedFunctionTool =
      new SchematizedFunctionTool(
        description,
        name,
        TapirSchemaToJsonSchema(implicitly[TSchema[T]], markOptionsAsNullable = true),
        None
      )

    def apply[T: TSchema](description: String, name: String, strict: Option[Boolean]): SchematizedFunctionTool =
      new SchematizedFunctionTool(
        description,
        name,
        TapirSchemaToJsonSchema(implicitly[TSchema[T]], markOptionsAsNullable = true),
        strict
      )
  }

  implicit val schemaRW: SnakePickle.ReadWriter[Schema] = SchemaSupport.schemaRW

  implicit val schematizedFunctionToolRW: SnakePickle.ReadWriter[SchematizedFunctionTool] = SnakePickle
    .readwriter[Value]
    .bimap[SchematizedFunctionTool](
      functionTool => {
        val obj = Obj(
          "description" -> functionTool.description,
          "name" -> functionTool.name,
          "parameters" -> SnakePickle.writeJs(functionTool.parameters)
        )
        functionTool.strict.foreach(flag => obj("strict") = Bool(flag))
        obj
      },
      json => {
        val strictOpt = json.obj.get("strict").map(_.bool)
        SchematizedFunctionTool(
          json("description").str,
          json("name").str,
          SnakePickle.read[Schema](json("parameters")),
          strictOpt
        )
      }
    )

  implicit val functionToolRW: SnakePickle.ReadWriter[FunctionTool] = SnakePickle
    .readwriter[Value]
    .bimap[FunctionTool](
      functionTool => {
        val obj = Obj(
          "description" -> functionTool.description,
          "name" -> functionTool.name,
          "parameters" -> functionTool.parameters
        )
        functionTool.strict.foreach(flag => obj("strict") = Bool(flag))
        obj
      },
      json => {
        val strictOpt = json.obj.get("strict").map(_.bool)
        FunctionTool(json("description").str, json("name").str, json("parameters").obj.toMap, strictOpt)
      }
    )

  /** Code interpreter tool
    *
    * The type of tool being defined: code_interpreter
    */
  case object CodeInterpreterTool extends Tool

  /** file_search tool
    *
    * The type of tool being defined: file_search
    */
  case object FileSearchTool extends Tool

  implicit val toolRW: SnakePickle.ReadWriter[Tool] = SnakePickle
    .readwriter[Value]
    .bimap[Tool](
      {
        case schematizedFunctionTool: SchematizedFunctionTool =>
          Obj("type" -> "function", "function" -> SnakePickle.writeJs(schematizedFunctionTool))
        case functionTool: FunctionTool =>
          Obj("type" -> "function", "function" -> SnakePickle.writeJs(functionTool))
        case CodeInterpreterTool =>
          Obj("type" -> "code_interpreter")
        case FileSearchTool =>
          Obj("type" -> "file_search")
      },
      json =>
        json("type").str match {
          case "function"         => SnakePickle.read[FunctionTool](json("function"))
          case "code_interpreter" => CodeInterpreterTool
          case "file_search"      => FileSearchTool
        }
    )
}
