package sttp.openai.requests.audio

import sttp.openai.json.SnakePickle

sealed abstract class RecognitionModel(val value: String)

object RecognitionModel {
  case object Whisper1 extends RecognitionModel("whisper-1")

  /** Use only as a workaround if API supports a format that's not yet predefined as a case object of Model. Otherwise, a custom format
    * would be rejected. See [[https://platform.openai.com/docs/api-reference/audio]] for current list of supported formats
    */
  case class Custom(customModel: String) extends RecognitionModel(customModel)

  val values: Set[RecognitionModel] = Set(Whisper1)

  implicit val ModelW: SnakePickle.Writer[RecognitionModel] = SnakePickle
    .writer[ujson.Value]
    .comap(_.value)
}
