package sttp.openai.requests.models

import sttp.openai.json.SnakePickle

object ModelsResponseData {

  case class ModelData(
      id: String,
      `object`: String,
      created: Int,
      ownedBy: String,
      permission: Seq[ModelPermission],
      root: String,
      parent: Option[String]
  )

  object ModelData {
    implicit def dataReadWriter: SnakePickle.ReadWriter[ModelData] = SnakePickle.macroRW[ModelData]
  }

  case class ModelPermission(
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

  object ModelPermission {
    implicit def permissionReadWriter: SnakePickle.ReadWriter[ModelPermission] = SnakePickle.macroRW[ModelPermission]
  }

  case class ModelsResponse(`object`: String, data: Seq[ModelData])

  object ModelsResponse {
    implicit def modelsResponseReadWriter: SnakePickle.ReadWriter[ModelsResponse] = SnakePickle.macroRW[ModelsResponse]
  }
}
