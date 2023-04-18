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
      batchSize: Option[String],
      promptLossWeight: Double,
      learningRateMultiplier: Option[String]
  )
  object Hyperparams {
    implicit val hyperparamsReadWriter: SnakePickle.ReadWriter[Hyperparams] = SnakePickle.macroRW[Hyperparams]
  }

  case class FineTuneResponse(
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
      fineTunedModel: Option[String],
      events: Seq[Event]
  )
  object FineTuneResponse {
    implicit val fineTuneResponseReadWriter: SnakePickle.ReadWriter[FineTuneResponse] = SnakePickle.macroRW[FineTuneResponse]
  }
}
