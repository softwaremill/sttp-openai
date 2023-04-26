package sttp.openai

import sttp.client4._
import sttp.model.Uri
import sttp.openai.json.SttpUpickleApiExtension.{asJsonSnake, upickleBodySerializerSnake}
import sttp.openai.requests.completions.CompletionsRequestBody.CompletionsBody
import sttp.openai.requests.completions.CompletionsResponseData.CompletionsResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.edit.EditRequestBody.EditBody
import sttp.openai.requests.completions.edit.EditRequestResponseData.EditResponse
import sttp.openai.requests.embeddings.EmbeddingsRequestBody.EmbeddingsBody
import sttp.openai.requests.embeddings.EmbeddingsResponseBody.EmbeddingResponse
import sttp.openai.requests.files.FilesResponseData._
import sttp.openai.requests.finetunes.FineTunesRequestBody
import sttp.openai.requests.finetunes.FineTunesResponseData.{
  DeleteFineTuneModelResponse,
  FineTuneEventsResponse,
  FineTuneResponse,
  GetFineTunesResponse
}
import sttp.openai.requests.images.ImageResponseData.ImageResponse
import sttp.openai.requests.images.creation.ImageCreationRequestBody.ImageCreationBody
import sttp.openai.requests.images.edit.ImageEditsConfig
import sttp.openai.requests.images.variations.ImageVariationsConfig
import sttp.openai.requests.models.ModelsResponseData.{ModelData, ModelsResponse}
import sttp.openai.requests.moderations.ModerationsRequestBody.ModerationsBody
import sttp.openai.requests.moderations.ModerationsResponseData.ModerationData
import sttp.openai.requests.audio.AudioResponseData.AudioResponse
import sttp.openai.requests.audio.transcriptions.TranscriptionConfig
import sttp.openai.requests.audio.translations.TranslationConfig
import sttp.openai.requests.audio.RecognitionModel

import java.io.File
import java.nio.file.Paths

class OpenAi(authToken: String) {

  /** Fetches all available models from [[https://platform.openai.com/docs/api-reference/models]] */
  def getModels: Request[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApiAuthRequest
      .get(OpenAIUris.Models)
      .response(asJsonSnake[ModelsResponse])

  /** @param modelId
    *   a Model's Id as String
    *
    * Fetches an available model for given modelId from [[https://platform.openai.com/docs/api-reference/models/{modelId}]]
    */
  def retrieveModel(modelId: String): Request[Either[ResponseException[String, Exception], ModelData]] =
    openApiAuthRequest
      .get(OpenAIUris.model(modelId))
      .response(asJsonSnake[ModelData])

  /** @param completionBody
    *   Request body
    *
    * Creates a completion for the provided prompt and parameters given in request body. More info:
    * [[https://platform.openai.com/docs/api-reference/completions/create]]
    */
  def createCompletion(completionBody: CompletionsBody): Request[Either[ResponseException[String, Exception], CompletionsResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Completions)
      .body(completionBody)
      .response(asJsonSnake[CompletionsResponse])

  /** @param imageCreationBody
    *   Create image request body
    *
    * Creates an image given a prompt in request body. More info: [[https://platform.openai.com/docs/api-reference/images/create]]
    */
  def createImage(imageCreationBody: ImageCreationBody): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.CreateImage)
      .body(imageCreationBody)
      .response(asJsonSnake[ImageResponse])

  /** Creates edited or extended images given an original image and a prompt
    * @param image
    *   JSON Lines image to be edited. <p> Must be a valid PNG file, less than 4MB, and square. If mask is not provided, image must have
    *   transparency, which will be used as the mask
    * @param prompt
    *   A text description of the desired image(s). The maximum length is 1000 characters.
    * @return
    *   An url to edited image.
    */
  def imageEdits(image: File, prompt: String): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.EditImage)
      .multipartBody(
        multipart("prompt", prompt),
        multipartFile("image", image)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates edited or extended images given an original image and a prompt
    *
    * @param systemPath
    *   SystemPath of the JSON Lines image to be edited. <p> Must be a valid PNG file, less than 4MB, and square. If mask is not provided,
    *   image must have transparency, which will be used as the mask
    *
    * @param prompt
    *   A text description of the desired image(s). The maximum length is 1000 characters.
    * @return
    *   An url to edited image.
    */
  def imageEdits(systemPath: String, prompt: String): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.EditImage)
      .multipartBody(
        multipart("prompt", prompt),
        multipartFile("image", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates edited or extended images given an original image and a prompt
    *
    * @param imageEditsConfig
    *   An instance of the case class ImageEditConfig containing the necessary parameters for editing the image
    *   - image: A file representing the image to be edited.
    *   - prompt: A string describing the desired edits to be made to the image.
    *   - mask: An optional file representing a mask to be applied to the image.
    *   - n: An optional integer specifying the number of edits to be made.
    *   - size: An optional instance of the Size case class representing the desired size of the output image.
    *   - responseFormat: An optional instance of the ResponseFormat case class representing the desired format of the response.
    * @return
    *   An url to edited image.
    */
  def imageEdits(
      imageEditsConfig: ImageEditsConfig
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.EditImage)
      .multipartBody {
        import imageEditsConfig._
        Seq(
          Some(multipartFile("image", image)),
          Some(multipart("prompt", prompt)),
          mask.map(multipartFile("mask", _)),
          n.map(multipart("n", _)),
          size.map(s => multipart("size", s.value)),
          responseFormat.map(format => multipart("response_format", format.value))
        ).flatten
      }
      .response(asJsonSnake[ImageResponse])

  /** Creates variations of a given image
    *
    * @param image
    *   File of the JSON Lines base image. <p> Must be a valid PNG file, less than 4MB, and square.
    * @return
    *   An url to edited image.
    */
  def imageVariations(
      image: File
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.VariationsImage)
      .multipartBody(
        multipartFile("image", image)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates variations of a given image
    *
    * @param systemPath
    *   SystemPath of the JSON Lines base image. <p> Must be a valid PNG file, less than 4MB, and square.
    * @return
    *   An url to edited image.
    */
  def imageVariations(
      systemPath: String
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.VariationsImage)
      .multipartBody(
        multipartFile("image", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates variations of a given image
    *
    * @param imageVariationsConfig
    *   An instance of the case class ImageVariationsConfig containing the necessary parameters for the image variation
    *   - image: A file of base image.
    *   - n: An optional integer specifying the number of images to generate.
    *   - size: An optional instance of the Size case class representing the desired size of the output image.
    *   - responseFormat: An optional instance of the ResponseFormat case class representing the desired format of the response.
    *   - user: An optional, unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
    * @return
    *   An url to edited image.
    */
  def imageVariations(
      imageVariationsConfig: ImageVariationsConfig
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.VariationsImage)
      .multipartBody {
        import imageVariationsConfig._
        Seq(
          Some(multipartFile("image", image)),
          n.map(multipart("n", _)),
          size.map(s => multipart("size", s.value)),
          responseFormat.map(format => multipart("response_format", format.value)),
          user.map(multipart("user", _))
        ).flatten
      }
      .response(asJsonSnake[ImageResponse])

  /** @param editRequestBody
    *   Edit request body
    *
    * Creates a new edit for provided request body. More info: [[https://platform.openai.com/docs/api-reference/edits/create]]
    */
  def createEdit(editRequestBody: EditBody): Request[Either[ResponseException[String, Exception], EditResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Edits)
      .body(editRequestBody)
      .response(asJsonSnake[EditResponse])

  /** @param chatBody
    *   Chat request body
    *
    * Creates a completion for the chat message given in request body. More info:
    * [[https://platform.openai.com/docs/api-reference/chat/create]]
    */
  def createChatCompletion(chatBody: ChatBody): Request[Either[ResponseException[String, Exception], ChatResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.ChatCompletions)
      .body(chatBody)
      .response(asJsonSnake[ChatResponse])

  /** Fetches all files that belong to the user's organization from [[https://platform.openai.com/docs/api-reference/files]] */
  def getFiles: Request[Either[ResponseException[String, Exception], FilesResponse]] =
    openApiAuthRequest
      .get(OpenAIUris.Files)
      .response(asJsonSnake[FilesResponse])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    * @param file
    *   JSON Lines file to be uploaded. <p> If the purpose is set to "fine-tune", each line is a JSON record with "prompt" and "completion"
    *   fields representing your [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @param purpose
    *   The intended purpose of the uploaded documents. <p> Use "fine-tune" for Fine-tuning. This allows OpenAI to validate the format of
    *   the uploaded file.
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(file: File, purpose: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", purpose),
        multipartFile("file", file)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * @param file
    *   JSON Lines file to be uploaded and the purpose is set to "fine-tune", each line is a JSON record with "prompt" and "completion"
    *   fields representing your [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(file: File): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", "fine-tune"),
        multipartFile("file", file)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * @param systemPath
    *   Path to the JSON Lines file to be uploaded. <p> If the purpose is set to "fine-tune", each line is a JSON record with "prompt" and
    *   "completion" fields representing your
    *   [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @param purpose
    *   The intended purpose of the uploaded documents. <p> Use "fine-tune" for Fine-tuning. This allows OpenAI to validate the format of
    *   the uploaded file.
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(systemPath: String, purpose: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", purpose),
        multipartFile("file", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * @param systemPath
    *   Path to the JSON Lines file to be uploaded and the purpose is set to "fine-tune", each line is a JSON record with "prompt" and
    *   "completion" fields representing your
    *   [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @return
    *   Uploaded file's basic information.
    */
  def uploadFile(systemPath: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", "fine-tune"),
        multipartFile("file", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[FileData])

  /** @param fileId
    *   The ID of the file to use for this request.
    * @return
    *   Information about deleted file.
    */
  def deleteFile(fileId: String): Request[Either[ResponseException[String, Exception], DeletedFileData]] =
    openApiAuthRequest
      .delete(OpenAIUris.file(fileId))
      .response(asJsonSnake[DeletedFileData])

  /** @param fileId
    *   The ID of the file to use for this request.
    * @return
    *   Returns information about a specific file.
    */
  def retrieveFile(fileId: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .get(OpenAIUris.file(fileId))
      .response(asJsonSnake[FileData])

  /** @param fileId
    *   The ID of the file.
    * @return
    *   the contents of the specified file.
    */
  def retrieveFileContent(fileId: String): Request[Either[String, String]] =
    openApiAuthRequest
      .get(OpenAIUris.fileContent(fileId))
      .response(asString)

  /** Translates audio into into English
    *
    * @param file
    *   File The audio file to translate, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    * @return
    *   Transcription of recorded audio into text.
    */
  def createTranslation(file: File, model: RecognitionModel): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Translations)
      .multipartBody(
        multipartFile("file", file),
        multipart("model", model.value)
      )
      .response(asJsonSnake[AudioResponse])

  /** Translates audio into into English
    *
    * @param systemPath
    *   The audio systemPath to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    * @return
    *   Transcription of recorded audio into text.
    */
  def createTranslation(systemPath: String, model: RecognitionModel): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Translations)
      .multipartBody(
        multipartFile("file", Paths.get(systemPath).toFile),
        multipart("model", model)
      )
      .response(asJsonSnake[AudioResponse])

  /** Translates audio into into English
    *
    * @param translationConfig
    *   An instance of the case class TranslationConfig containing the necessary parameters for the audio translation
    *   - file: The audio file to translate, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    *   - model: ID of the model to use. Only whisper-1 is currently available.
    *   - prompt: An optional text to guide the model's style or continue a previous audio segment. The prompt should be in English.
    *   - responseFormat: An optional instance of the ResponseFormat case class representing the desired format of the response.
    *   - temperature: An optional sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while
    *     lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use log probability to
    *     automatically increase the temperature until certain thresholds are hit.
    * @return
    *   An url to edited image.
    */
  def createTranslation(translationConfig: TranslationConfig): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Translations)
      .multipartBody {
        import translationConfig._
        Seq(
          Some(multipartFile("file", file)),
          Some(multipart("model", model.value)),
          prompt.map(multipart("prompt", _)),
          responseFormat.map(format => multipart("response_format", format)),
          temperature.map(multipart("temperature", _))
        ).flatten
      }
      .response(asJsonSnake[AudioResponse])

  /** @param moderationsBody
    *   Moderation request body.
    * @return
    *   Classifies if text violates OpenAI's Content Policy
    */
  def createModeration(moderationsBody: ModerationsBody) =
    openApiAuthRequest
      .post(OpenAIUris.Moderations)
      .body(moderationsBody)
      .response(asJsonSnake[ModerationData])

  /** Transcribes audio into the input language
    *
    * @param file
    *   The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    * @return
    *   Transcription of recorded audio into text.
    */

  def createTranscription(file: File, model: RecognitionModel): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Transcriptions)
      .multipartBody(
        multipartFile("file", file),
        multipart("model", model.value)
      )
      .response(asJsonSnake[AudioResponse])

  /** Transcribes audio into the input language
    *
    * @param systemPath
    *   The audio systemPath to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    * @return
    *   Transcription of recorded audio into text.
    */
  def createTranscription(
      systemPath: String,
      model: RecognitionModel
  ): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Transcriptions)
      .multipartBody(
        multipartFile("file", Paths.get(systemPath).toFile),
        multipart("model", model.value)
      )
      .response(asJsonSnake[AudioResponse])

  /** Transcribes audio into the input language
    *
    * @param transcriptionConfig
    *   An instance of the case class TranscriptionConfig containing the necessary parameters for the audio transcription
    *   - file: The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    *   - model: ID of the model to use. Only whisper-1 is currently available.
    *   - prompt: An optional text to guide the model's style or continue a previous audio segment. The prompt should match the audio
    *     language.
    *   - responseFormat: An optional instance of the ResponseFormat case class representing the desired format of the response.
    *   - temperature: An optional sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while
    *     lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use log probability to
    *     automatically increase the temperature until certain thresholds are hit.
    *     - language: An optional language of the input audio. Supplying the input language in ISO-639-1 format will improve accuracy and
    *       latency.
    * @return
    *   An url to edited image.
    */
  def createTranscription(transcriptionConfig: TranscriptionConfig): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.Transcriptions)
      .multipartBody {
        import transcriptionConfig._
        Seq(
          Some(multipartFile("file", file)),
          Some(multipart("model", model.value)),
          prompt.map(multipart("prompt", _)),
          responseFormat.map(format => multipart("response_format", format.value)),
          temperature.map(multipart("temperature", _)),
          language.map(multipart("language", _))
        ).flatten
      }
      .response(asJsonSnake[AudioResponse])

  /** Creates a job that fine-tunes a specified model from a given dataset.
    * @param fineTunesRequestBody
    *   Request body that will be used to create a fine-tune.
    * @return
    *   Details of the enqueued job including job status and the name of the fine-tuned models once complete.
    */
  def createFineTune(
      fineTunesRequestBody: FineTunesRequestBody
  ): Request[Either[ResponseException[String, Exception], FineTuneResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.FineTunes)
      .body(fineTunesRequestBody)
      .response(asJsonSnake[FineTuneResponse])

  /** @return
    *   List of your organization's fine-tuning jobs.
    */
  def getFineTunes: Request[Either[ResponseException[String, Exception], GetFineTunesResponse]] =
    openApiAuthRequest
      .get(OpenAIUris.FineTunes)
      .response(asJsonSnake[GetFineTunesResponse])

  /** Immediately cancel a fine-tune job.
    *
    * @param fineTuneId
    *   The ID of the fine-tune job to cancel.
    * @return
    *   Cancelled fine-tune job.
    */
  def cancelFineTune(fineTuneId: String): Request[Either[ResponseException[String, Exception], FineTuneResponse]] =
    openApiAuthRequest
      .post(OpenAIUris.cancelFineTune(fineTuneId))
      .response(asJsonSnake[FineTuneResponse])

  /** @param embeddingsBody
    *   Embeddings request body.
    * @return
    *   An embedding vector representing the input text.
    */
  def createEmbeddings(embeddingsBody: EmbeddingsBody) =
    openApiAuthRequest
      .post(OpenAIUris.Embeddings)
      .body(embeddingsBody)
      .response(asJsonSnake[EmbeddingResponse])

  /** @param fineTuneId
    *   The ID of the fine-tune job.
    * @return
    *   Info about the fine-tune job.
    */
  def retrieveFineTune(fineTuneId: String): Request[Either[ResponseException[String, Exception], FineTuneResponse]] =
    openApiAuthRequest
      .get(OpenAIUris.fineTune(fineTuneId))
      .response(asJsonSnake[FineTuneResponse])

  /** Delete a fine-tuned model. You must have the Owner role in your organization.
    *
    * @param model
    *   The model to delete.
    * @return
    *   Deleted fine-tuned model information.
    */
  def deleteFineTuneModel(model: String): Request[Either[ResponseException[String, Exception], DeleteFineTuneModelResponse]] =
    openApiAuthRequest
      .delete(OpenAIUris.fineTuneModel(model))
      .response(asJsonSnake[DeleteFineTuneModelResponse])

  /** @param fineTuneId
    *   The ID of the fine-tune job to get events for.
    * @return
    *   Fine-grained status updates for a fine-tune job.
    */
  def getFineTuneEvents(fineTuneId: String): Request[Either[ResponseException[String, Exception], FineTuneEventsResponse]] =
    openApiAuthRequest
      .get(OpenAIUris.fineTuneEvents(fineTuneId))
      .response(asJsonSnake[FineTuneEventsResponse])

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIUris {
  private val ImageBase: Uri = uri"https://api.openai.com/v1/images"
  private val AudioBase: Uri = uri"https://api.openai.com/v1/audio/"

  val ChatCompletions: Uri = uri"https://api.openai.com/v1/chat/completions"
  val Completions: Uri = uri"https://api.openai.com/v1/completions"
  val CreateImage: Uri = ImageBase.addPath("generations")
  val Edits: Uri = uri"https://api.openai.com/v1/edits"
  val Embeddings: Uri = uri"https://api.openai.com/v1/embeddings"
  val EditImage: Uri = ImageBase.addPath("edits")
  val Files: Uri = uri"https://api.openai.com/v1/files"
  val FineTunes: Uri = uri"https://api.openai.com/v1/fine-tunes"
  val Models: Uri = uri"https://api.openai.com/v1/models"
  val Moderations: Uri = uri"https://api.openai.com/v1/moderations"
  val Transcriptions: Uri = AudioBase.addPath("transcriptions")
  val Translations: Uri = AudioBase.addPath("translations")
  val VariationsImage: Uri = ImageBase.addPath("variations")

  def cancelFineTune(fineTuneId: String): Uri = FineTunes.addPath(fineTuneId, "cancel")
  def file(fileId: String): Uri = Files.addPath(fileId)
  def fileContent(fileId: String): Uri = Files.addPath(fileId, "content")
  def fineTuneModel(model: String): Uri = Models.addPath(model)
  def fineTuneEvents(fineTuneId: String): Uri = FineTunes.addPath(fineTuneId, "events")
  def fineTune(fineTuneId: String): Uri = FineTunes.addPath(fineTuneId)
  def model(modelId: String): Uri = Models.addPath(modelId)
}
