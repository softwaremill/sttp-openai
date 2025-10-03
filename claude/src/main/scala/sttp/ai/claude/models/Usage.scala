package sttp.ai.claude.models

import sttp.ai.claude.json.SnakePickle.{macroRW, ReadWriter}

case class Usage(
    inputTokens: Int,
    outputTokens: Int
) {
  def totalTokens: Int = inputTokens + outputTokens
}

object Usage {
  implicit val rw: ReadWriter[Usage] = macroRW
}
