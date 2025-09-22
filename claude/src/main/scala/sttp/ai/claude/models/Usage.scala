package sttp.ai.claude.models

import upickle.default.{macroRW, ReadWriter}

case class Usage(
    inputTokens: Int,
    outputTokens: Int
) {
  def totalTokens: Int = inputTokens + outputTokens
}

object Usage {
  implicit val rw: ReadWriter[Usage] = macroRW
}
