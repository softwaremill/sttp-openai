package sttp.openai.requests.vectorstore.file

import sttp.openai.json.SnakePickle

object VectorStoreFileRequestBody {

  case class CreateVectorStoreFileBody(
      fileId: String
  )

  object CreateVectorStoreFileBody {
    implicit val createVectorStoreFileBodyR: SnakePickle.Writer[CreateVectorStoreFileBody] = SnakePickle.macroW[CreateVectorStoreFileBody]
  }

  case class ListVectorStoreFilesBody(
      limit: Int = 20,
      order: String = "desc",
      after: Option[String] = None,
      before: Option[String] = None,
      filter: Option[FileStatus] = None
  ) {
    def toMap: Map[String, String] = {
      val map = Map("limit" -> limit.toString, "order" -> order)
      map ++
        after.map("after" -> _) ++
        before.map("before" -> _) ++
        filter.map("filter" -> _.toString)
    }
  }

  object ListVectorStoreFilesBody {
    implicit val listVectorStoreFilesBodyR: SnakePickle.Writer[ListVectorStoreFilesBody] = SnakePickle.macroW[ListVectorStoreFilesBody]
  }
}
