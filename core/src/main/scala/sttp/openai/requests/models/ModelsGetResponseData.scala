package sttp.openai.requests.models

import sttp.openai.json.SnakePickle

object ModelsGetResponseData {

  case class Data(
      id: String,
      `object`: String,
      created: Int,
      ownedBy: String,
      permission: Seq[Permission],
      root: String,
      parent: Option[String]
  )

  object Data {
    implicit def dataReadWriter: SnakePickle.ReadWriter[Data] = SnakePickle.macroRW[Data]
  }

  case class Permission(
      id: String,
      `object`: String,
      created: Int,
      allowCreateEngine: Boolean,
      allowSampling: Boolean,
      allowLogprobs: Boolean,
      allowSearchIndices: Boolean,
      allowView: Boolean,
      allowFineTuning: Boolean,
      organization: String,
      group: Option[String],
      isBlocking: Boolean
  )

  object Permission {
    implicit def permissionReadWriter: SnakePickle.ReadWriter[Permission] = SnakePickle.macroRW[Permission]
  }

  case class ModelsResponse(`object`: String, data: Seq[Data])

  object ModelsResponse {
    implicit def modelsResponseReadWriter: SnakePickle.ReadWriter[ModelsResponse] = SnakePickle.macroRW[ModelsResponse]
  }
}
