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
    implicit val eventsBodyReadWriter: SnakePickle.ReadWriter[Event] = SnakePickle.macroRW[Event]
  }

  case class Hyperparams(
      nEpochs: Int,
      batchSize: Option[Int],
      promptLossWeight: Double,
      learningRateMultiplier: Option[Double]
  )
  object Hyperparams {
    implicit val hyperparamsReadWriter: SnakePickle.ReadWriter[Hyperparams] = SnakePickle.macroRW[Hyperparams]
  }

  case class CreateFineTuneResponse(
      fineTuneData: FineTuneData,
      events: Seq[Event]
  )
  object CreateFineTuneResponse {
    implicit val fineTuneResponseReader: SnakePickle.Reader[CreateFineTuneResponse] =
      SnakePickle.reader[ujson.Value].map[CreateFineTuneResponse] { jsonValue =>
        val fineTuneData: FineTuneData = SnakePickle.read[FineTuneData](jsonValue)
        val events: Seq[Event] = SnakePickle.read[Seq[Event]](jsonValue("events"))
        CreateFineTuneResponse(fineTuneData, events)
      }
  }

  case class GetFineTunesResponse(`object`: String, data: Seq[FineTuneData])

  object GetFineTunesResponse {
    implicit val getFineTunesResponseReadWriter: SnakePickle.ReadWriter[GetFineTunesResponse] = SnakePickle.macroRW[GetFineTunesResponse]
  }
  case class FineTuneData(
      `object`: String,
      id: String,
      hyperparams: Hyperparams,
      organizationId: String,
      model: String,
      trainingFiles: Seq[FileData],
      validationFiles: Seq[FileData],
      resultFiles: Seq[FileData],
      createdAt: Int,
      updatedAt: Int,
      status: String,
      fineTunedModel: Option[String]
  )
  object FineTuneData {
    implicit val fineTuneDataReadWriter: SnakePickle.ReadWriter[FineTuneData] = SnakePickle.macroRW[FineTuneData]
  }
}
