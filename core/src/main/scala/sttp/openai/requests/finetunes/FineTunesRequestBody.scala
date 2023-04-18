package sttp.openai.requests.finetunes

import sttp.openai.json.SnakePickle

case class FineTunesRequestBody(
    trainingFile: String,
    validationFile: Option[String] = None,
    model: Option[String] = None,
    nEpochs: Option[Int] = None,
    batchSize: Option[Int] = None,
    learningRateMultiplier: Option[Double] = None,
    promptLossWeight: Option[Double] = None,
    computeClassificationMetrics: Option[Boolean] = None,
    classificationNClasses: Option[Int] = None,
    classificationPositiveClass: Option[String] = None,
    classificationBetas: Option[Array[Double]] = None,
    suffix: Option[String] = None
)
object FineTunesRequestBody {
  implicit val fineTunesRequestBodyReadWriter: SnakePickle.ReadWriter[FineTunesRequestBody] = SnakePickle.macroRW[FineTunesRequestBody]
}
