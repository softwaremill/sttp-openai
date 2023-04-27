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
    implicit def dataReader: SnakePickle.Reader[ModelData] = SnakePickle.macroR
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
    implicit def permissionR: SnakePickle.Reader[ModelPermission] = SnakePickle.macroR
  }

  case class ModelsResponse(`object`: String, data: Seq[ModelData])

  object ModelsResponse {
    implicit def modelsResponseR: SnakePickle.Reader[ModelsResponse] = SnakePickle.macroR
  }
}
