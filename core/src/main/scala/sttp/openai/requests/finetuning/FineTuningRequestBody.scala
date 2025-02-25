package sttp.openai.requests.finetuning

import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.json.SnakePickle
import ujson.Str

/** @param model
  *   The name of the model to fine-tune. You can select one of the supported models
  *   [[https://platform.openai.com/docs/guides/fine-tuning#which-models-can-be-fine-tuned]].
  * @param trainingFile
  *   The ID of an uploaded file that contains training data. See upload file for how to upload a file. Your dataset must be formatted as a
  *   JSONL file. Additionally, you must upload your file with the purpose fine-tune. The contents of the file should differ depending on if
  *   the model uses the chat, completions format, or if the fine-tuning method uses the preference format. See the fine-tuning guide for
  *   more details.
  * @param suffix
  *   A string of up to 64 characters that will be added to your fine-tuned model name. For example, a suffix of "custom-model-name" would
  *   produce a model name like ft:gpt-4o-mini:openai:custom-model-name:7p4lURel.
  * @param validationFile
  *   The ID of an uploaded file that contains validation data. If you provide this file, the data is used to generate validation metrics
  *   periodically during fine-tuning. These metrics can be viewed in the fine-tuning results file. The same data should not be present in
  *   both train and validation files. Your dataset must be formatted as a JSONL file. You must upload your file with the purpose fine-tune.
  *   See the fine-tuning guide for more details.
  * @param integrations
  *   A list of integrations to enable for your fine-tuning job.
  * @param seed
  *   The seed controls the reproducibility of the job. Passing in the same seed and job parameters should produce the same results, but may
  *   differ in rare cases. If a seed is not specified, one will be generated for you.
  * @param method
  *   The method used for fine-tuning.
  */
case class FineTuningRequestBody(
    model: FineTuningModel,
    trainingFile: String,
    suffix: Option[String] = None,
    validationFile: Option[String] = None,
    integrations: Option[Seq[Integration]] = None,
    seed: Option[Int] = None,
    method: Option[Method] = None
)
object FineTuningRequestBody {
  implicit val fineTuningRequestBodyWriter: SnakePickle.Writer[FineTuningRequestBody] = SnakePickle.macroW[FineTuningRequestBody]
}

sealed abstract class FineTuningModel(val value: String)

object FineTuningModel {

  implicit val fineTuningModelRW: SnakePickle.ReadWriter[FineTuningModel] = SnakePickle
    .readwriter[ujson.Value]
    .bimap[FineTuningModel](
      model => SnakePickle.writeJs(model.value),
      jsonValue =>
        SnakePickle.read[ujson.Value](jsonValue) match {
          case Str(value) =>
            byFineTuningModelValue.getOrElse(value, CustomFineTuningModel(value))
          case e => throw DeserializationOpenAIException(new Exception(s"Could not deserialize: $e"))
        }
    )

  case object GPT4o20240806 extends FineTuningModel("gpt-4o-2024-08-06")

  case object GPT4oMini20240718 extends FineTuningModel("gpt-4o-mini-2024-07-18")

  case object GPT40613 extends FineTuningModel("gpt-4-0613")

  case object GPT35Turbo0125 extends FineTuningModel("gpt-3.5-turbo-0125")

  case object GPT35Turbo1106 extends FineTuningModel("gpt-3.5-turbo-1106")

  case object GPT35Turbo0613 extends FineTuningModel("gpt-3.5-turbo-0613")

  case class CustomFineTuningModel(customFineTuningModel: String) extends FineTuningModel(customFineTuningModel)

  val values: Set[FineTuningModel] = Set(GPT4o20240806, GPT4oMini20240718, GPT40613, GPT35Turbo0125, GPT35Turbo1106, GPT35Turbo0613)

  private val byFineTuningModelValue = values.map(model => model.value -> model).toMap

}
