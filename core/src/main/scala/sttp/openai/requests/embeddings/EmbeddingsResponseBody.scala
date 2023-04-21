package sttp.openai.requests.embeddings

import sttp.openai.json.SnakePickle

object EmbeddingsResponseBody {
  case class EmbeddingData(
      `object`: String,
      index: Int,
      embedding: Seq[Double]
  )
  object EmbeddingData {
    implicit val embeddingDataReader: SnakePickle.Reader[EmbeddingData] = SnakePickle.macroR[EmbeddingData]
  }

  case class EmbeddingResponse(
      `object`: String,
      data: Seq[EmbeddingData],
      model: String,
      usage: Usage
  )

  object EmbeddingResponse {
    implicit val embeddingResponseDataReader: SnakePickle.Reader[EmbeddingResponse] = SnakePickle.macroR[EmbeddingResponse]
  }
  case class Usage(
      promptTokens: Int,
      totalTokens: Int
  )

  object Usage {
    implicit val usageDataReader: SnakePickle.Reader[Usage] = SnakePickle.macroR[Usage]
  }
}
