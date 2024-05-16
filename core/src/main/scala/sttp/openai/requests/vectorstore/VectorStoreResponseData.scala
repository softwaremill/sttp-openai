package sttp.openai.requests.vectorstore

import sttp.openai.json.SnakePickle
import ujson.Value

object VectorStoreResponseData {

  /** Represents a vector store object.
    *
    * @param id
    *   The identifier, which can be referenced in API endpoints.
    * @param `object`
    *   The object type, which is always vector_store.
    * @param createdAt
    *   The Unix timestamp (in seconds) for when the vector store was created.
    * @param name
    *   The name of the vector store.
    * @param usageBytes
    *   The total number of bytes used by the files in the vector store.
    * @param fileCounts
    *   Object containing file count properties.
    * @param status
    *   The status of the vector store.
    * @param expiresAfter
    *   The expiration policy for a vector store.
    * @param metadata
    *   Set of key-value pairs that can be attached to an object.
    */
  case class VectorStore(
      id: String,
      `object`: String = "vector_store",
      createdAt: Int,
      name: String,
      usageBytes: Int,
      fileCounts: FileCounts,
      status: StoreStatus,
      expiresAfter: Option[ExpiresAfter] = None,
      expiresAt: Option[Int] = None,
      lastActiveAt: Option[Int] = None,
      lastUsedAt: Option[Int] = None,
      metadata: Map[String, String] = Map.empty
  )

  object VectorStore {
    implicit val vectorStoreR: SnakePickle.Reader[VectorStore] = SnakePickle.macroR[VectorStore]
  }

  /** Describes number of files in different statuses.
    *
    * @param inProgress
    *   The number of files currently in progress.
    * @param completed
    *   The number of files that have been completed successfully.
    * @param failed
    *   The number of files that have failed.
    * @param cancelled
    *   The number of files that have been cancelled.
    * @param total
    *   The total number of files.
    */
  case class FileCounts(
      inProgress: Int,
      completed: Int,
      failed: Int,
      cancelled: Int,
      total: Int
  )

  object FileCounts {
    implicit val fileCountsR: SnakePickle.Reader[FileCounts] = SnakePickle.macroR[FileCounts]
  }

  sealed trait StoreStatus
  case object InProgress extends StoreStatus
  case object Completed extends StoreStatus
  case object Expired extends StoreStatus

  object StoreStatus {
    implicit val storeStatusRW: SnakePickle.Reader[StoreStatus] = SnakePickle
      .reader[Value]
      .map(json =>
        json.str match {
          case "in_progress" => InProgress
          case "completed"   => Completed
          case "expired"     => Expired
        }
      )
  }

  case class ListVectorStoresResponse(
      `object`: String = "list",
      data: Seq[VectorStore],
      firstId: String,
      lastId: String,
      hasMore: Boolean
  )

  object ListVectorStoresResponse {
    implicit val listVectorStoresResponseR: SnakePickle.Reader[ListVectorStoresResponse] = SnakePickle.macroR[ListVectorStoresResponse]
  }

  case class DeleteVectorStoreResponse(
      id: String,
      `object`: String,
      deleted: Boolean
  )

  object DeleteVectorStoreResponse {
    implicit val deleteVectorStoreResponseR: SnakePickle.Reader[DeleteVectorStoreResponse] = SnakePickle.macroR[DeleteVectorStoreResponse]
  }
}
