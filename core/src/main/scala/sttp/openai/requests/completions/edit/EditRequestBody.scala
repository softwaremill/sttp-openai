package sttp.openai.requests.completions.edit

import sttp.openai.json.SnakePickle

object EditRequestBody {

  case class EditBody(
      model: String,
      input: Option[String] = None,
      instruction: String,
      n: Option[Int] = None,
      temperature: Option[Double] = None,
      topP: Option[Double] = None
  )

  object EditBody {
    implicit val editBodyRW: SnakePickle.ReadWriter[EditBody] = SnakePickle.macroRW[EditBody]
  }

}
