package sttp.openai

import sttp.client4._
import sttp.model.Uri
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.json.SttpUpickleApiExtension.{asJsonSnake, asStreamSnake, asStringEither, upickleBodySerializer}
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
import sttp.capabilities.Streams

import java.io.File
import java.nio.file.Paths

class OpenAI(authToken: String) {

  /** Lists the currently available models, and provides basic information about each one such as the owner and availability.
    *
    * [[https://platform.openai.com/docs/api-reference/models]]
    */
  def getModels: Request[Either[OpenAIException, ModelsResponse]] =
    openAIAuthRequest
      .get(OpenAIUris.Models)
      .response(asJsonSnake[ModelsResponse])

  /** Retrieves a model instance, providing basic information about the model such as the owner and permissions.
    *
    * [[https://platform.openai.com/docs/api-reference/models/retrieve]]
    *
    * @param modelId
    *   The ID of the model to use for this request.
    */
  def retrieveModel(modelId: String): Request[Either[OpenAIException, ModelData]] =
    openAIAuthRequest
      .get(OpenAIUris.model(modelId))
      .response(asJsonSnake[ModelData])

  /** Creates a completion for the provided prompt and parameters given in request body.
    *
    * [[https://platform.openai.com/docs/api-reference/completions/create]]
    *
    * @param completionBody
    *   Create completion request body.
    */
  def createCompletion(completionBody: CompletionsBody): Request[Either[OpenAIException, CompletionsResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Completions)
      .body(completionBody)
      .response(asJsonSnake[CompletionsResponse])

  /** Creates an image given a prompt in request body.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create]]
    *
    * @param imageCreationBody
    *   Create image request body.
    */
  def createImage(imageCreationBody: ImageCreationBody): Request[Either[OpenAIException, ImageResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.CreateImage)
      .body(imageCreationBody)
      .response(asJsonSnake[ImageResponse])

  /** Creates edited or extended images given an original image and a prompt.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-edit]]
    *
    * @param image
    *   The image to be edited.
    *
    * Must be a valid PNG file, less than 4MB, and square. If mask is not provided, image must have transparency, which will be used as the
    * mask.
    * @param prompt
    *   A text description of the desired image. The maximum length is 1000 characters.
    */
  def imageEdits(image: File, prompt: String): Request[Either[OpenAIException, ImageResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.EditImage)
      .multipartBody(
        multipart("prompt", prompt),
        multipartFile("image", image)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates edited or extended images given an original image and a prompt.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-edit]]
    *
    * @param systemPath
    *   Path to the image to be edited.
    *
    * Must be a valid PNG file, less than 4MB, and square. If mask is not provided, image must have transparency, which will be used as the
    * mask
    * @param prompt
    *   A text description of the desired image. The maximum length is 1000 characters.
    */
  def imageEdits(systemPath: String, prompt: String): Request[Either[OpenAIException, ImageResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.EditImage)
      .multipartBody(
        multipart("prompt", prompt),
        multipartFile("image", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates edited or extended images given an original image and a prompt.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-edit]]
    *
    * @param imageEditsConfig
    *   An instance of the case class ImageEditConfig containing the necessary parameters for editing the image.
    */
  def imageEdits(
      imageEditsConfig: ImageEditsConfig
  ): Request[Either[OpenAIException, ImageResponse]] =
    openAIAuthRequest
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

  /** Creates a variation of a given image.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-variation]]
    *
    * @param image
    *   The image to use as the basis for the variation.
    *
    * Must be a valid PNG file, less than 4MB, and square.
    */
  def imageVariations(
      image: File
  ): Request[Either[OpenAIException, ImageResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.VariationsImage)
      .multipartBody(
        multipartFile("image", image)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates a variation of a given image.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-variation]]
    *
    * @param systemPath
    *   Path to the image to use as the basis for the variation.
    *
    * Must be a valid PNG file, less than 4MB, and square.
    */
  def imageVariations(
      systemPath: String
  ): Request[Either[OpenAIException, ImageResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.VariationsImage)
      .multipartBody(
        multipartFile("image", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates a variation of a given image.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-variation]]
    *
    * @param imageVariationsConfig
    *   An instance of the case class ImageVariationsConfig containing the necessary parameters for the image variation.
    */
  def imageVariations(
      imageVariationsConfig: ImageVariationsConfig
  ): Request[Either[OpenAIException, ImageResponse]] =
    openAIAuthRequest
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

  /** Creates a new edit for provided request body.
    *
    * [[https://platform.openai.com/docs/api-reference/edits/create]]
    *
    * @param editRequestBody
    *   Edit request body.
    */
  def createEdit(editRequestBody: EditBody): Request[Either[OpenAIException, EditResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Edits)
      .body(editRequestBody)
      .response(asJsonSnake[EditResponse])

  /** Creates a model response for the given chat conversation defined in chatBody.
    *
    * [[https://platform.openai.com/docs/api-reference/chat/create]]
    *
    * @param chatBody
    *   Chat request body.
    */
  def createChatCompletion(chatBody: ChatBody): Request[Either[OpenAIException, ChatResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.ChatCompletions)
      .body(chatBody)
      .response(asJsonSnake[ChatResponse])

    /** Creates and streams a model response for the given chat conversation defined in chatBody.
      *
      * [[https://platform.openai.com/docs/api-reference/chat/create]]
      *
      * @param s
      *   The streams implementation to use.
      * @param chatBody
      *   Chat request body.
      */
    def createChatCompletion[S](s: Streams[S], chatBody: ChatBody): StreamRequest[Either[OpenAIException, s.BinaryStream], S] =
      openAIAuthRequest
        .post(OpenAIUris.ChatCompletions)
        .body(chatBody)
        .response(asStreamSnake(s))

  /** Returns a list of files that belong to the user's organization.
    *
    * [[https://platform.openai.com/docs/api-reference/files]]
    */
  def getFiles: Request[Either[OpenAIException, FilesResponse]] =
    openAIAuthRequest
      .get(OpenAIUris.Files)
      .response(asJsonSnake[FilesResponse])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * [[https://platform.openai.com/docs/api-reference/files/upload]]
    *
    * @param file
    *   JSON Lines file to be uploaded.
    *
    * If the purpose is set to "fine-tune", each line is a JSON record with "prompt" and "completion" fields representing your
    * [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @param purpose
    *   The intended purpose of the uploaded documents.
    *
    * Use "fine-tune" for Fine-tuning. This allows OpenAI to validate the format of the uploaded file.
    */
  def uploadFile(file: File, purpose: String): Request[Either[OpenAIException, FileData]] =
    openAIAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", purpose),
        multipartFile("file", file)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * [[https://platform.openai.com/docs/api-reference/files/upload]]
    *
    * @param file
    *   JSON Lines file to be uploaded and the purpose is set to "fine-tune", each line is a JSON record with "prompt" and "completion"
    *   fields representing your [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    */
  def uploadFile(file: File): Request[Either[OpenAIException, FileData]] =
    openAIAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", "fine-tune"),
        multipartFile("file", file)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * [[https://platform.openai.com/docs/api-reference/files/upload]]
    *
    * @param systemPath
    *   Path to the JSON Lines file to be uploaded.
    *
    * If the purpose is set to "fine-tune", each line is a JSON record with "prompt" and "completion" fields representing your
    * [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    * @param purpose
    *   The intended purpose of the uploaded documents.
    *
    * Use "fine-tune" for Fine-tuning. This allows OpenAI to validate the format of the uploaded file.
    */
  def uploadFile(systemPath: String, purpose: String): Request[Either[OpenAIException, FileData]] =
    openAIAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", purpose),
        multipartFile("file", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[FileData])

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * [[https://platform.openai.com/docs/api-reference/files/upload]]
    *
    * @param systemPath
    *   Path to the JSON Lines file to be uploaded and the purpose is set to "fine-tune", each line is a JSON record with "prompt" and
    *   "completion" fields representing your
    *   [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    */
  def uploadFile(systemPath: String): Request[Either[OpenAIException, FileData]] =
    openAIAuthRequest
      .post(OpenAIUris.Files)
      .multipartBody(
        multipart("purpose", "fine-tune"),
        multipartFile("file", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[FileData])

  /** Delete a file.
    *
    * [[https://platform.openai.com/docs/api-reference/files/delete]]
    *
    * @param fileId
    *   The ID of the file to use for this request.
    */
  def deleteFile(fileId: String): Request[Either[OpenAIException, DeletedFileData]] =
    openAIAuthRequest
      .delete(OpenAIUris.file(fileId))
      .response(asJsonSnake[DeletedFileData])

  /** Returns information about a specific file.
    *
    * [[https://platform.openai.com/docs/api-reference/files/retrieve]]
    *
    * @param fileId
    *   The ID of the file to use for this request.
    */
  def retrieveFile(fileId: String): Request[Either[OpenAIException, FileData]] =
    openAIAuthRequest
      .get(OpenAIUris.file(fileId))
      .response(asJsonSnake[FileData])

  /** Returns the contents of the specified file.
    *
    * [[https://platform.openai.com/docs/api-reference/files/retrieve-content]]
    *
    * @param fileId
    *   The ID of the file.
    */
  def retrieveFileContent(fileId: String): Request[Either[OpenAIException, String]] =
    openAIAuthRequest
      .get(OpenAIUris.fileContent(fileId))
      .response(asStringEither)

  /** Translates audio into English text.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param file
    *   The audio file to translate, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranslation(file: File, model: RecognitionModel): Request[Either[OpenAIException, AudioResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Translations)
      .multipartBody(
        multipartFile("file", file),
        multipart("model", model.value)
      )
      .response(asJsonSnake[AudioResponse])

  /** Translates audio into English text.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param systemPath
    *   The audio systemPath to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranslation(systemPath: String, model: RecognitionModel): Request[Either[OpenAIException, AudioResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Translations)
      .multipartBody(
        multipartFile("file", Paths.get(systemPath).toFile),
        multipart("model", model)
      )
      .response(asJsonSnake[AudioResponse])

  /** Translates audio into English text.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param translationConfig
    *   An instance of the case class TranslationConfig containing the necessary parameters for the audio translation.
    */
  def createTranslation(translationConfig: TranslationConfig): Request[Either[OpenAIException, AudioResponse]] =
    openAIAuthRequest
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

  /** Classifies if text violates OpenAI's Content Policy.
    *
    * [[https://platform.openai.com/docs/api-reference/moderations/create]]
    *
    * @param moderationsBody
    *   Moderation request body.
    */
  def createModeration(moderationsBody: ModerationsBody): Request[Either[OpenAIException, ModerationData]] =
    openAIAuthRequest
      .post(OpenAIUris.Moderations)
      .body(moderationsBody)
      .response(asJsonSnake[ModerationData])

  /** Transcribes audio into the input language.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param file
    *   The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranscription(file: File, model: RecognitionModel): Request[Either[OpenAIException, AudioResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Transcriptions)
      .multipartBody(
        multipartFile("file", file),
        multipart("model", model.value)
      )
      .response(asJsonSnake[AudioResponse])

  /** Transcribes audio into the input language.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param systemPath
    *   The audio systemPath to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranscription(
      systemPath: String,
      model: RecognitionModel
  ): Request[Either[OpenAIException, AudioResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Transcriptions)
      .multipartBody(
        multipartFile("file", Paths.get(systemPath).toFile),
        multipart("model", model.value)
      )
      .response(asJsonSnake[AudioResponse])

  /** Transcribes audio into the input language.
    *
    * @param transcriptionConfig
    *   An instance of the case class TranscriptionConfig containing the necessary parameters for the audio transcription
    * @return
    *   An url to edited image.
    */
  def createTranscription(transcriptionConfig: TranscriptionConfig): Request[Either[OpenAIException, AudioResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Transcriptions)
      .multipartBody {
        import transcriptionConfig._
        Seq(
          Some(multipartFile("file", file)),
          Some(multipart("model", model.value)),
          prompt.map(multipart("prompt", _)),
          responseFormat.map(format => multipart("response_format", format.value)),
          temperature.map(multipart("temperature", _)),
          language.map(lang => multipart("language", lang.value))
        ).flatten
      }
      .response(asJsonSnake[AudioResponse])

  /** Creates a job that fine-tunes a specified model from a given dataset.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/create]]
    *
    * @param fineTunesRequestBody
    *   Request body that will be used to create a fine-tune.
    */
  def createFineTune(
      fineTunesRequestBody: FineTunesRequestBody
  ): Request[Either[OpenAIException, FineTuneResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.FineTunes)
      .body(fineTunesRequestBody)
      .response(asJsonSnake[FineTuneResponse])

  /** List of your organization's fine-tuning jobs.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/list]]
    */
  def getFineTunes: Request[Either[OpenAIException, GetFineTunesResponse]] =
    openAIAuthRequest
      .get(OpenAIUris.FineTunes)
      .response(asJsonSnake[GetFineTunesResponse])

  /** Immediately cancel a fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/cancel]]
    *
    * @param fineTuneId
    *   The ID of the fine-tune job to cancel.
    */
  def cancelFineTune(fineTuneId: String): Request[Either[OpenAIException, FineTuneResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.cancelFineTune(fineTuneId))
      .response(asJsonSnake[FineTuneResponse])

  /** Gets info about the fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/embeddings/create]]
    *
    * @param embeddingsBody
    *   Embeddings request body.
    */
  def createEmbeddings(embeddingsBody: EmbeddingsBody): Request[Either[OpenAIException, EmbeddingResponse]] =
    openAIAuthRequest
      .post(OpenAIUris.Embeddings)
      .body(embeddingsBody)
      .response(asJsonSnake[EmbeddingResponse])

  /** Gets info about the fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/retrieve]]
    *
    * @param fineTuneId
    *   The ID of the fine-tune job.
    */
  def retrieveFineTune(fineTuneId: String): Request[Either[OpenAIException, FineTuneResponse]] =
    openAIAuthRequest
      .get(OpenAIUris.fineTune(fineTuneId))
      .response(asJsonSnake[FineTuneResponse])

  /** Delete a fine-tuned model. You must have the Owner role in your organization.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/delete-model]]
    *
    * @param model
    *   The model to delete.
    */
  def deleteFineTuneModel(model: String): Request[Either[OpenAIException, DeleteFineTuneModelResponse]] =
    openAIAuthRequest
      .delete(OpenAIUris.fineTuneModel(model))
      .response(asJsonSnake[DeleteFineTuneModelResponse])

  /** Get fine-grained status updates for a fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/events]]
    *
    * @param fineTuneId
    *   The ID of the fine-tune job to get events for.
    */
  def getFineTuneEvents(fineTuneId: String): Request[Either[OpenAIException, FineTuneEventsResponse]] =
    openAIAuthRequest
      .get(OpenAIUris.fineTuneEvents(fineTuneId))
      .response(asJsonSnake[FineTuneEventsResponse])

  private val openAIAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
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
