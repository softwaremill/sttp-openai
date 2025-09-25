package sttp.ai.claude.responses

import sttp.ai.claude.json.SnakePickle.{macroRW, ReadWriter}

case class ModelsResponse(
    data: List[ModelData]
)

case class ModelData(
    id: String,
    `type`: String,
    displayName: String
)

object ModelData {
  implicit val rw: ReadWriter[ModelData] = macroRW
}

object ModelsResponse {
  implicit val rw: ReadWriter[ModelsResponse] = macroRW
}
