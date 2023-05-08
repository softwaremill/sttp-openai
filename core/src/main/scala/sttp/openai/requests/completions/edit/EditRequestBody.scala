package sttp.openai.requests.completions.edit

import sttp.openai.json.{DeserializationException, SnakePickle}
import ujson.Str

object EditRequestBody {

  /** @param model
    *   ID of the [[EditModel]] to use.
    * @param input
    *   The input text to use as a starting point for the edit.
    * @param instruction
    *   The instruction that tells the model how to edit the prompt.
    * @param n
    *   How many edits to generate for the input and instruction.
    * @param temperature
    *   What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like
    *   0.2 will make it more focused and deterministic.
    * @param topP
    *   An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p
    *   probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
    */
  case class EditBody(
      model: EditModel,
      input: Option[String] = None,
      instruction: String,
      n: Option[Int] = None,
      temperature: Option[Double] = None,
      topP: Option[Double] = None
  )

  object EditBody {
    implicit val editBodyW: SnakePickle.Writer[EditBody] = SnakePickle.macroW[EditBody]
  }

  sealed abstract class EditModel(val value: String)

  object EditModel {
    implicit val editModelRW: SnakePickle.ReadWriter[EditModel] = SnakePickle
      .readwriter[ujson.Value]
      .bimap[EditModel](
        model => SnakePickle.writeJs(model.value),
        jsonValue =>
          SnakePickle.read[ujson.Value](jsonValue) match {
            case Str(value) =>
              byEditModelValue.getOrElse(value, throw new DeserializationException(new Exception(s"Could not serialize: $value")))
            case e => throw new DeserializationException(new Exception(s"Could not serialize: $e"))
          }
      )

    case object TextDavinciEdit001 extends EditModel("text-davinci-edit-001")
    case object CodeDavinciEdit001 extends EditModel("code-davinci-edit-001")
    case class CustomEditModel(customEditModel: String) extends EditModel(customEditModel)

    val values: Set[EditModel] = Set(TextDavinciEdit001, CodeDavinciEdit001)

    private val byEditModelValue = values.map(model => model.value -> model).toMap
  }

}
