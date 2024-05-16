package sttp.openai.requests.vectorstore.file

import sttp.openai.json.SnakePickle
import ujson.Value

object VectorStoreFileResponseData {

  /** Represents a vector store object.
    *
    * @param id
    *   The identifier, which can be referenced in API endpoints.
    * @param `object`
    *   The object type, which is always vector_store.
    * @param createdAt
    *   The Unix timestamp (in seconds) for when the vector store was created.
    * @param usageBytes
    *   The total number of bytes used by the files in the vector store.
    */
  case class VectorStoreFile(
      id: String,
      `object`: String = "vector_store.file",
      usageBytes: Int,
      createdAt: Int,
      vectorStoreId: String,
      status: FileStatus,
      lastError: Option[LastError] = None
  )

  object VectorStoreFile {
    implicit val vectorStoreFileR: SnakePickle.Reader[VectorStoreFile] = SnakePickle.macroR[VectorStoreFile]
  }

  case class LastError(code: ErrorCode, message: String)

  object LastError {
    implicit val lastErrorR: SnakePickle.Reader[LastError] = SnakePickle.macroR[LastError]
  }

  sealed trait ErrorCode

  case object ServerError extends ErrorCode

  case object RateLimitExceeded extends ErrorCode

  object ErrorCode {
    implicit val errorCodeR: SnakePickle.Reader[ErrorCode] = SnakePickle
      .reader[Value]
      .map(json =>
        json.str match {
          case "server_error"        => ServerError
          case "rate_limit_exceeded" => RateLimitExceeded
        }
      )
  }

  case class ListVectorStoreFilesResponse(
      `object`: String = "list",
      data: Seq[VectorStoreFile],
      firstId: String,
      lastId: String,
      hasMore: Boolean
  )

  object ListVectorStoreFilesResponse {
    implicit val listVectorStoreFilesResponseR: SnakePickle.Reader[ListVectorStoreFilesResponse] =
      SnakePickle.macroR[ListVectorStoreFilesResponse]
  }

  case class DeleteVectorStoreFileResponse(
      id: String,
      `object`: String,
      deleted: Boolean
  )

  object DeleteVectorStoreFileResponse {
    implicit val deleteVectorStoreFileResponseR: SnakePickle.Reader[DeleteVectorStoreFileResponse] =
      SnakePickle.macroR[DeleteVectorStoreFileResponse]
  }

}
