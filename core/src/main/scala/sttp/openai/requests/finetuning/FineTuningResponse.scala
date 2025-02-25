package sttp.openai.requests.finetuning

import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.json.SnakePickle
import ujson.Str

case class FineTuningResponse(
    id: String,
    createdAt: Int,
    error: Option[Error] = None,
    fineTunedModel: Option[String],
    finishedAt: Option[Int],
    hyperparameters: Option[Hyperparameters],
    model: String,
    `object`: String,
    organizationId: String,
    resultFiles: Seq[String],
    status: Status,
    trainedTokens: Option[Int] = None,
    trainingFile: String,
    validationFile: Option[String],
    integrations: Option[Seq[Integration]] = None,
    seed: Int,
    estimatedFinish: Option[Int] = None,
    method: Method
)

object FineTuningResponse {
  implicit val fineTuningResponseDataReader: SnakePickle.Reader[FineTuningResponse] = SnakePickle.macroR[FineTuningResponse]
}

/** @param code
  *   A machine-readable error code.
  * @param message
  *   A human-readable error message.
  * @param param
  *   The parameter that was invalid, usually training_file or validation_file. This field will be null if the failure was not
  *   parameter-specific.
  */
case class Error(
    code: String,
    message: String,
    param: Option[String] = None
)

object Error {
  implicit val errorReader: SnakePickle.Reader[Error] = SnakePickle.macroR[Error]
}

sealed abstract class Status(val value: String)

object Status {

  implicit val statusRW: SnakePickle.Reader[Status] = SnakePickle
    .reader[ujson.Value]
    .map[Status](
      jsonValue =>
        SnakePickle.read[ujson.Value](jsonValue) match {
          case Str(value) => byStatusValue.getOrElse(value, CustomStatus(value))
          case e          => throw DeserializationOpenAIException(new Exception(s"Could not deserialize: $e"))
        }
    )

  case object ValidatingFiles extends Status("validating_files")

  case object Queued extends Status("queued")

  case object Running extends Status("running")

  case object Succeeded extends Status("succeeded")

  case object Failed extends Status("failed")

  case object Cancelled extends Status("cancelled")

  case class CustomStatus(customStatus: String) extends Status(customStatus)

  private val values: Set[Status] = Set(ValidatingFiles, Queued, Running, Succeeded, Failed, Cancelled)

  private val byStatusValue = values.map(status => status.value -> status).toMap

}
