package sttp.openai.requests.finetunes

import sttp.openai.json.SnakePickle
import sttp.openai.requests.files.FilesResponseData.FileData

import scala.collection.immutable.Seq

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
      batchSize: Option[String],
      promptLossWeight: Double,
      learningRateMultiplier: Option[String]
  )
  object Hyperparams {
    implicit val hyperparamsReadWriter: SnakePickle.ReadWriter[Hyperparams] = SnakePickle.macroRW[Hyperparams]
  }

  case class FineTuneResponse(
      fineTuneData: FineTuneData,
      events: Seq[Event]
  )
  object FineTuneResponse {
//    implicit val fineTuneResponseReadWriter: SnakePickle.ReadWriter[FineTuneResponse] = // SnakePickle.macroRW[FineTuneResponse]
//      SnakePickle.readwriter[ujson.Value].bimap[FineTuneResponse] {
//        fineTuneResponse =>
//        , jsonValue => ???
//      }
    implicit val fineTuneResponseReader: SnakePickle.Reader[FineTuneResponse] =
      SnakePickle.reader[String].map[FineTuneResponse] { jsonValue =>
        val fineTuneData: FineTuneData = SnakePickle.read[FineTuneData](jsonValue)
        val events: Event = SnakePickle.read[Event](jsonValue)
        FineTuneResponse(fineTuneData, Seq(events))
      }
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
