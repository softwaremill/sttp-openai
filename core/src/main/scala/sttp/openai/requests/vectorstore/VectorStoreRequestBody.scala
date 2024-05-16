package sttp.openai.requests.vectorstore

import sttp.openai.json.SnakePickle

object VectorStoreRequestBody {

  case class CreateVectorStoreBody(
      fileIds: Option[Seq[String]] = None,
      name: Option[String] = None,
      expiresAfter: Option[ExpiresAfter] = None,
      metadata: Option[Map[String, String]] = None
  )

  object CreateVectorStoreBody {
    implicit val createVectorStoreBodyW: SnakePickle.Writer[CreateVectorStoreBody] = SnakePickle.macroW[CreateVectorStoreBody]
  }

  case class RetrieveVectorStoreBody(
      vectorStoreId: String
  )

  object RetrieveVectorStoreBody {
    implicit val retrieveVectorStoreBody: SnakePickle.Writer[RetrieveVectorStoreBody] = SnakePickle.macroW[RetrieveVectorStoreBody]
  }

  case class ModifyVectorStoreBody(
      name: Option[String] = None,
      expiresAfter: Option[ExpiresAfter] = None,
      metadata: Option[Map[String, String]] = None
  )

  object ModifyVectorStoreBody {
    implicit val modifyVectorStoreBody: SnakePickle.Writer[ModifyVectorStoreBody] = SnakePickle.macroW[ModifyVectorStoreBody]
  }

}
