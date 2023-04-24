package sttp.openai.requests.completions

import sttp.openai.json.SnakePickle

case class Usage(promptTokens: Int, completionTokens: Int, totalTokens: Int)

object Usage {
  implicit val choicesR: SnakePickle.Reader[Usage] = SnakePickle.macroR[Usage]
}
