package sttp.openai.requests.finetunes

import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.json.SnakePickle
import ujson.Str

/** @param trainingFile
  *   The ID of an uploaded file that contains training data.
  * @param validationFile
  *   The ID of an uploaded file that contains validation data.
  * @param model
  *   The name of the base model to fine-tune.
  * @param nEpochs
  *   The number of epochs to train the model for. An epoch refers to one full cycle through the training dataset.
  * @param batchSize
  *   The batch size to use for training. The batch size is the number of training examples used to train a single forward and backward
  *   pass.
  * @param learningRateMultiplier
  *   The learning rate multiplier to use for training. The fine-tuning learning rate is the original learning rate used for pretraining
  *   multiplied by this value.
  * @param promptLossWeight
  *   The weight to use for loss on the prompt tokens. This controls how much the model tries to learn to generate the prompt (as compared
  *   to the completion which always has a weight of 1.0), and can add a stabilizing effect to training when completions are short.
  * @param computeClassificationMetrics
  *   If set, we calculate classification-specific metrics such as accuracy and F-1 score using the validation set at the end of every
  *   epoch. These metrics can be viewed in the results file.<p> In order to compute classification metrics, you must provide a
  *   [[validationFile]]. Additionally, you must specify [[classificationNClasses]] for multiclass classification or
  *   [[classificationPositiveClass]] for binary classification.
  * @param classificationNClasses
  *   The number of classes in a classification task.
  * @param classificationPositiveClass
  *   The positive class in binary classification.
  * @param classificationBetas
  *   If this is provided, we calculate F-beta scores at the specified beta values. The F-beta score is a generalization of F-1 score. This
  *   is only used for binary classification.
  * @param suffix
  *   A string of up to 40 characters that will be added to your fine-tuned model name.
  */
case class FineTunesRequestBody(
    trainingFile: String,
    validationFile: Option[String] = None,
    model: Option[FineTuneModel] = None,
    nEpochs: Option[Int] = None,
    batchSize: Option[Int] = None,
    learningRateMultiplier: Option[Double] = None,
    promptLossWeight: Option[Double] = None,
    computeClassificationMetrics: Option[Boolean] = None,
    classificationNClasses: Option[Int] = None,
    classificationPositiveClass: Option[String] = None,
    classificationBetas: Option[Seq[Double]] = None,
    suffix: Option[String] = None
)
object FineTunesRequestBody {
  implicit val fineTunesRequestBodyWriter: SnakePickle.Writer[FineTunesRequestBody] = SnakePickle.macroW[FineTunesRequestBody]
}

sealed abstract class FineTuneModel(val value: String)

object FineTuneModel {

  implicit val fineTuneModelRW: SnakePickle.ReadWriter[FineTuneModel] = SnakePickle
    .readwriter[ujson.Value]
    .bimap[FineTuneModel](
      model => SnakePickle.writeJs(model.value),
      jsonValue =>
        SnakePickle.read[ujson.Value](jsonValue) match {
          case Str(value) =>
            byFineTuneModelValue.getOrElse(value, CustomFineTuneModel(value))
          case e => throw DeserializationOpenAIException(new Exception(s"Could not deserialize: $e"))
        }
    )

  case object Davinci extends FineTuneModel("davinci")
  case object Curie extends FineTuneModel("curie")
  case object Babbage extends FineTuneModel("babbage")
  case object Ada extends FineTuneModel("ada")

  case class CustomFineTuneModel(customFineTuneModel: String) extends FineTuneModel(customFineTuneModel)

  val values: Set[FineTuneModel] = Set(Davinci, Curie, Babbage, Ada)

  private val byFineTuneModelValue = values.map(model => model.value -> model).toMap
}
