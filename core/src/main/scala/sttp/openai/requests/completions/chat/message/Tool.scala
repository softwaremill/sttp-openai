package sttp.openai.requests.completions.chat.message

import sttp.apispec.Schema
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.SchemaSupport
import sttp.tapir.docs.apispec.schema.TapirSchemaToJsonSchema
import sttp.tapir.{Schema => TSchema}
import ujson._

sealed trait Tool

object Tool {

  /** @param description
    *   A description of what the function does, used by the model to choose when and how to call the function.
    * @param name
    *   The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
    * @param parameters
    *   The parameters the functions accepts, described as a JSON Schema object
    */
  case class FunctionTool(description: String, name: String, parameters: Map[String, Value]) extends Tool

  /** With this class we are not forced to provide Json Schema by hand. It will be automatically generated based on the type T. Please refer
    * to readme for example of usage.
    */
  case class SchematizedFunctionTool(description: String, name: String, parameters: Schema) extends Tool

  object SchematizedFunctionTool {
    def apply[T: TSchema](description: String, name: String): SchematizedFunctionTool =
      new SchematizedFunctionTool(
        description,
        name,
        TapirSchemaToJsonSchema(implicitly[TSchema[T]], markOptionsAsNullable = true)
      )
  }

  implicit val schemaRW: SnakePickle.ReadWriter[Schema] = SchemaSupport.schemaRW

  implicit val schematizedFunctionToolRW: SnakePickle.ReadWriter[SchematizedFunctionTool] = SnakePickle
    .readwriter[Value]
    .bimap[SchematizedFunctionTool](
      functionTool =>
        Obj(
          "description" -> functionTool.description,
          "name" -> functionTool.name,
          "parameters" -> SnakePickle.writeJs(functionTool.parameters)
        ),
      json => SchematizedFunctionTool(json("description").str, json("name").str, SnakePickle.read[Schema](json("parameters")))
    )

  implicit val functionToolRW: SnakePickle.ReadWriter[FunctionTool] = SnakePickle
    .readwriter[Value]
    .bimap[FunctionTool](
      functionTool => Obj("description" -> functionTool.description, "name" -> functionTool.name, "parameters" -> functionTool.parameters),
      json => FunctionTool(json("description").str, json("name").str, json("parameters").obj.toMap)
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
