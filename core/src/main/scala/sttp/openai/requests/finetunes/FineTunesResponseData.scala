package sttp.openai.requests.finetunes

import sttp.openai.json.SnakePickle
import sttp.openai.requests.files.FilesResponseData.FileData

object FineTunesResponseData {
  case class Event(
      `object`: String,
      level: String,
      message: String,
      createdAt: Int
  )
  object Event {
    implicit val eventsBodyReader: SnakePickle.Reader[Event] = SnakePickle.macroR[Event]
  }

  case class Hyperparams(
      nEpochs: Int,
      batchSize: Option[Int],
      promptLossWeight: Double,
      learningRateMultiplier: Option[Double]
  )
  object Hyperparams {
    implicit val hyperparamsReader: SnakePickle.Reader[Hyperparams] = SnakePickle.macroR[Hyperparams]
  }

  case class FineTuneResponse(
      fineTuneData: FineTuneData,
      events: Seq[Event]
  )
  object FineTuneResponse {
    implicit val fineTuneResponseReader: SnakePickle.Reader[FineTuneResponse] =
      SnakePickle.reader[ujson.Value].map[FineTuneResponse] { jsonValue =>
        val fineTuneData: FineTuneData = SnakePickle.read[FineTuneData](jsonValue)
        val events: Seq[Event] = SnakePickle.read[Seq[Event]](jsonValue("events"))
        FineTuneResponse(fineTuneData, events)
      }
  }

  case class GetFineTunesResponse(`object`: String, data: Seq[FineTuneData])

  object GetFineTunesResponse {
    implicit val getFineTunesResponseReader: SnakePickle.Reader[GetFineTunesResponse] = SnakePickle.macroR[GetFineTunesResponse]
  }
  case class FineTuneData(
      `object`: String,
      id: String,
      hyperparams: Hyperparams,
      organizationId: String,
      model: FineTuneModel,
      trainingFiles: Seq[FileData],
      validationFiles: Seq[FileData],
      resultFiles: Seq[FileData],
      createdAt: Int,
      updatedAt: Int,
      status: String,
      fineTunedModel: Option[String]
  )
  object FineTuneData {
    implicit val fineTuneDataReader: SnakePickle.Reader[FineTuneData] = SnakePickle.macroR[FineTuneData]
  }

  case class DeleteFineTuneModelResponse(id: String, `object`: String, deleted: Boolean)
  object DeleteFineTuneModelResponse {
    implicit val fineTuneDataReader: SnakePickle.Reader[DeleteFineTuneModelResponse] = SnakePickle.macroR[DeleteFineTuneModelResponse]
  }
  case class FineTuneEventsResponse(`object`: String, data: Seq[Event])
  object FineTuneEventsResponse {
    implicit val fineTuneDataReader: SnakePickle.Reader[FineTuneEventsResponse] = SnakePickle.macroR[FineTuneEventsResponse]
  }
}
