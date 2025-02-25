package sttp.openai.requests.finetuning

import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.json.SnakePickle
import sttp.openai.requests.finetuning.FineTuningRequestBody.Integration.Integration
import sttp.openai.requests.finetuning.FineTuningRequestBody.Method.Method
import ujson.Str

object FineTuningRequestBody {

  /** @param model
    *   The name of the model to fine-tune. You can select one of the supported models
    *   [[https://platform.openai.com/docs/guides/fine-tuning#which-models-can-be-fine-tuned]].
    * @param trainingFile
    *   The ID of an uploaded file that contains training data. See upload file for how to upload a file. Your dataset must be formatted as
    *   a JSONL file. Additionally, you must upload your file with the purpose fine-tune. The contents of the file should differ depending
    *   on if the model uses the chat, completions format, or if the fine-tuning method uses the preference format. See the fine-tuning
    *   guide for more details.
    * @param suffix
    *   A string of up to 64 characters that will be added to your fine-tuned model name. For example, a suffix of "custom-model-name" would
    *   produce a model name like ft:gpt-4o-mini:openai:custom-model-name:7p4lURel.
    * @param validationFile
    *   The ID of an uploaded file that contains validation data. If you provide this file, the data is used to generate validation metrics
    *   periodically during fine-tuning. These metrics can be viewed in the fine-tuning results file. The same data should not be present in
    *   both train and validation files. Your dataset must be formatted as a JSONL file. You must upload your file with the purpose
    *   fine-tune. See the fine-tuning guide for more details.
    * @param integrations
    *   A list of integrations to enable for your fine-tuning job.
    * @param seed
    *   The seed controls the reproducibility of the job. Passing in the same seed and job parameters should produce the same results, but
    *   may differ in rare cases. If a seed is not specified, one will be generated for you.
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

  sealed abstract class Type(val value: String)

  object Type {
    def typeRW(byTypeValue: Map[String, Type]): SnakePickle.ReadWriter[Type] = SnakePickle
      .readwriter[ujson.Value]
      .bimap[Type](
        `type` => SnakePickle.writeJs(`type`.value),
        jsonValue =>
          SnakePickle.read[ujson.Value](jsonValue) match {
            case Str(value) =>
              byTypeValue.get(value) match {
                case Some(t) => t
                case None    => throw DeserializationOpenAIException(new Exception(s"Could not deserialize: $value"))
              }
            case e => throw DeserializationOpenAIException(new Exception(s"Could not deserialize: $e"))
          }
      )
  }

  object Method {

    object MethodType {
      case object Supervised extends Type("supervised")

      case object Dpo extends Type("dpo")

      private val values: Set[Type] = Set(Supervised, Dpo)

      private val byTypeValue = values.map(`type` => `type`.value -> `type`).toMap

      implicit val typeRW: SnakePickle.ReadWriter[Type] = Type.typeRW(byTypeValue)
    }

    /** @param batchSize
      *   Number of examples in each batch. A larger batch size means that model parameters are updated less frequently, but with lower
      *   variance.
      * @param learningRateMultiplier
      *   Scaling factor for the learning rate. A smaller learning rate may be useful to avoid overfitting.
      * @param nEpochs
      *   The number of epochs to train the model for. An epoch refers to one full cycle through the training dataset.
      * @param beta
      *   The beta value for the DPO method. A higher beta value will increase the weight of the penalty between the policy and reference
      *   model.
      */
    case class Hyperparameters(
        batchSize: Option[Int] = None,
        learningRateMultiplier: Option[Float] = None,
        nEpochs: Option[Int] = None,
        beta: Option[Float] = None
    )

    /** @param hyperparameters
      *   The hyperparameters used for the fine-tuning job.
      */
    case class Supervised(
        hyperparameters: Option[Hyperparameters] = None
    )

    /** @param hyperparameters
      *   The hyperparameters used for the fine-tuning job.
      */
    case class Dpo(
        hyperparameters: Option[Hyperparameters] = None
    )

    /** @param `type`
      *   The type of method. Is either supervised or dpo.
      * @param supervised
      *   Configuration for the supervised fine-tuning method.
      * @param dpo
      *   Configuration for the DPO fine-tuning method.
      */
    case class Method(
        `type`: Option[Type] = None,
        supervised: Option[Supervised] = None,
        dpo: Option[Dpo] = None
    )
  }

  object Integration {

    object IntegrationType {
      case object Wandb extends Type("wandb")

      private val values: Set[Type] = Set(Wandb)

      private val byTypeValue = values.map(`type` => `type`.value -> `type`).toMap

      implicit val typeRW: SnakePickle.ReadWriter[Type] = Type.typeRW(byTypeValue)
    }

    /** @param project
      *   The name of the project that the new run will be created under.
      * @param name
      *   A display name to set for the run. If not set, we will use the Job ID as the name.
      * @param entity
      *   The entity to use for the run. This allows you to set the team or username of the WandB user that you would like associated with
      *   the run. If not set, the default entity for the registered WandB API key is used.
      * @param tags
      *   A list of tags to be attached to the newly created run. These tags are passed through directly to WandB. Some default tags are
      *   generated by OpenAI: "openai/finetune", "openai/{base-model}", "openai/{ftjob-abcdef}".
      */
    case class Wandb(
        project: String,
        name: Option[String] = None,
        entity: Option[String] = None,
        tags: Option[Seq[String]]
    )

    /** @param `type`
      *   The type of integration to enable. Currently, only "wandb" (Weights and Biases) is supported.
      * @param wandb
      *   The settings for your integration with Weights and Biases. This payload specifies the project that metrics will be sent to.
      *   Optionally, you can set an explicit display name for your run, add tags to your run, and set a default entity (team, username,
      *   etc) to be associated with your run.
      */
    case class Integration(
        `type`: Type,
        wandb: Wandb
    )
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

}
