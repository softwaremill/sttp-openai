package sttp.openai.requests.completions.edit

import sttp.openai.json.SnakePickle

object EditRequestBody {

  /** @param model
    *   ID of the model to use. You can use the `text-davinci-edit-001` or `code-davinci-edit-001` model with this endpoint.
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
      model: String,
      input: Option[String] = None,
      instruction: String,
      n: Option[Int] = None,
      temperature: Option[Double] = None,
      topP: Option[Double] = None
  )

  object EditBody {
    implicit val editBodyW: SnakePickle.Writer[EditBody] = SnakePickle.macroW
  }
}
