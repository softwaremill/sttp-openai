package sttp.openai

import sttp.client4._
import sttp.model.{Header, Uri}
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.json.SttpUpickleApiExtension.{asJsonSnake, asStreamSnake, asStringEither, upickleBodySerializer}
import sttp.openai.requests.assistants.AssistantsRequestBody.{CreateAssistantBody, CreateAssistantFileBody, ModifyAssistantBody}
import sttp.openai.requests.assistants.AssistantsResponseData.{
  AssistantData,
  AssistantFileData,
  DeleteAssistantFileResponse,
  DeleteAssistantResponse,
  ListAssistantFilesResponse,
  ListAssistantsResponse
}
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
import sttp.openai.requests.threads.ThreadsRequestBody.CreateThreadBody
import sttp.openai.requests.threads.ThreadsResponseData.{DeleteThreadResponse, ThreadData}
import sttp.openai.requests.threads.messages.ThreadMessagesRequestBody.CreateMessage
import sttp.openai.requests.threads.messages.ThreadMessagesResponseData.{
  ListMessageFilesResponse,
  ListMessagesResponse,
  MessageData,
  MessageFileData
}
import sttp.openai.requests.threads.runs.ThreadRunsRequestBody.{
  CreateRun,
  CreateThreadAndRun,
  ModifyRun,
  SubmitToolOutputsToRun,
  ToolOutput
}
import sttp.openai.requests.threads.runs.ThreadRunsResponseData.{ListRunStepsResponse, ListRunsResponse, RunData, RunStepData}
import sttp.openai.requests.threads.QueryParameters

import java.io.File
import java.nio.file.Paths

class OpenAI(authToken: String, baseUri: Uri = OpenAIUris.OpenAIBaseUri) {

  private val openAIUris = new OpenAIUris(baseUri)

  /** Lists the currently available models, and provides basic information about each one such as the owner and availability.
    *
    * [[https://platform.openai.com/docs/api-reference/models]]
    */
  def getModels: Request[Either[OpenAIException, ModelsResponse]] =
    openAIAuthRequest
      .get(openAIUris.Models)
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
      .get(openAIUris.model(modelId))
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
      .post(openAIUris.Completions)
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
      .post(openAIUris.CreateImage)
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
      .post(openAIUris.EditImage)
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
      .post(openAIUris.EditImage)
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
      .post(openAIUris.EditImage)
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
      .post(openAIUris.VariationsImage)
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
      .post(openAIUris.VariationsImage)
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
      .post(openAIUris.VariationsImage)
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
      .post(openAIUris.Edits)
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
      .post(openAIUris.ChatCompletions)
      .body(chatBody)
      .response(asJsonSnake[ChatResponse])

  /** Creates and streams a model response as chunk objects for the given chat conversation defined in chatBody.
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
      .post(openAIUris.ChatCompletions)
      .body(ChatBody.withStreaming(chatBody))
      .response(asStreamSnake(s))

  /** Returns a list of files that belong to the user's organization.
    *
    * [[https://platform.openai.com/docs/api-reference/files]]
    */
  def getFiles: Request[Either[OpenAIException, FilesResponse]] =
    openAIAuthRequest
      .get(openAIUris.Files)
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
      .post(openAIUris.Files)
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
      .post(openAIUris.Files)
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
      .post(openAIUris.Files)
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
      .post(openAIUris.Files)
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
      .delete(openAIUris.file(fileId))
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
      .get(openAIUris.file(fileId))
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
      .get(openAIUris.fileContent(fileId))
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
      .post(openAIUris.Translations)
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
      .post(openAIUris.Translations)
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
      .post(openAIUris.Translations)
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
      .post(openAIUris.Moderations)
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
      .post(openAIUris.Transcriptions)
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
      .post(openAIUris.Transcriptions)
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
      .post(openAIUris.Transcriptions)
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
      .post(openAIUris.FineTunes)
      .body(fineTunesRequestBody)
      .response(asJsonSnake[FineTuneResponse])

  /** List of your organization's fine-tuning jobs.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/list]]
    */
  def getFineTunes: Request[Either[OpenAIException, GetFineTunesResponse]] =
    openAIAuthRequest
      .get(openAIUris.FineTunes)
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
      .post(openAIUris.cancelFineTune(fineTuneId))
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
      .post(openAIUris.Embeddings)
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
      .get(openAIUris.fineTune(fineTuneId))
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
      .delete(openAIUris.fineTuneModel(model))
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
      .get(openAIUris.fineTuneEvents(fineTuneId))
      .response(asJsonSnake[FineTuneEventsResponse])

  /** Create a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/createThread]]
    *
    * @param createThreadBody
    *   Create completion request body.
    */
  def createThread(createThreadBody: CreateThreadBody): Request[Either[OpenAIException, ThreadData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.Threads)
      .body(createThreadBody)
      .response(asJsonSnake[ThreadData])

  /** Retrieves a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/getThread]]
    *
    * @param threadId
    *   The ID of the thread to retrieve.
    */
  def retrieveThread(threadId: String): Request[Either[OpenAIException, ThreadData]] =
    betaOpenAIAuthRequest
      .get(openAIUris.thread(threadId))
      .response(asJsonSnake[ThreadData])

  /** Modifies a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/modifyThread]]
    *
    * @param threadId
    *   The ID of the thread to modify. Only the metadata can be modified.
    */
  def modifyThread(threadId: String, metadata: Map[String, String]): Request[Either[OpenAIException, ThreadData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.thread(threadId))
      .body(metadata)
      .response(asJsonSnake[ThreadData])

  /** Delete a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/deleteThread]]
    *
    * @param threadId
    *   The ID of the thread to delete.
    */
  def deleteThread(threadId: String): Request[Either[OpenAIException, DeleteThreadResponse]] =
    betaOpenAIAuthRequest
      .delete(openAIUris.thread(threadId))
      .response(asJsonSnake[DeleteThreadResponse])

  /** Create a message.
    *
    * [[https://platform.openai.com/docs/api-reference/messages/createMessage]]
    *
    * @param threadId
    *   The ID of the thread to create a message for.
    */
  def createThreadMessage(threadId: String, message: CreateMessage): Request[Either[OpenAIException, MessageData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.threadMessages(threadId))
      .body(message)
      .response(asJsonSnake[MessageData])

  /** Returns a list of messages for a given thread.
    *
    * [[https://platform.openai.com/docs/api-reference/messages/listMessages]]
    *
    * @param threadId
    *   The ID of the thread the messages belong to.
    */
  def listThreadMessages(
      threadId: String,
      queryParameters: QueryParameters = QueryParameters.empty
  ): Request[Either[OpenAIException, ListMessagesResponse]] = {
    val uri = openAIUris
      .threadMessages(threadId)
      .withParams(queryParameters.toMap)

    betaOpenAIAuthRequest
      .get(uri)
      .response(asJsonSnake[ListMessagesResponse])
  }

  /** Returns a list of message files.
    *
    * [[https://platform.openai.com/docs/api-reference/messages/listMessageFiles]]
    *
    * @param threadId
    *   The ID of the thread that the message and files belong to.
    *
    * @param messageId
    *   The ID of the message that the files belongs to.
    */
  def listThreadMessageFiles(
      threadId: String,
      messageId: String,
      queryParameters: QueryParameters = QueryParameters.empty
  ): Request[Either[OpenAIException, ListMessageFilesResponse]] = {
    val uri = openAIUris
      .threadMessageFiles(threadId, messageId)
      .withParams(queryParameters.toMap)

    betaOpenAIAuthRequest
      .get(uri)
      .response(asJsonSnake[ListMessageFilesResponse])
  }

  /** Retrieve a message.
    *
    * [[https://platform.openai.com/docs/api-reference/messages/getMessage]]
    *
    * @param threadId
    *   The ID of the thread to which this message belongs.
    *
    * @param messageId
    *   The ID of the message to retrieve.
    */
  def retrieveThreadMessage(
      threadId: String,
      messageId: String
  ): Request[Either[OpenAIException, MessageData]] =
    betaOpenAIAuthRequest
      .get(openAIUris.threadMessage(threadId, messageId))
      .response(asJsonSnake[MessageData])

  /** Retrieves a message file.
    *
    * [[https://platform.openai.com/docs/api-reference/messages/getMessageFile]]
    *
    * @param threadId
    *   The ID of the thread to which the message and File belong.
    *
    * @param messageId
    *   The ID of the message the file belongs to.
    *
    * @param fileId
    *   The ID of the file being retrieved.
    */
  def retrieveThreadMessageFile(
      threadId: String,
      messageId: String,
      fileId: String
  ): Request[Either[OpenAIException, MessageFileData]] =
    betaOpenAIAuthRequest
      .get(openAIUris.threadMessageFile(threadId, messageId, fileId))
      .response(asJsonSnake[MessageFileData])

  /** Modifies a message.
    *
    * [[https://platform.openai.com/docs/api-reference/messages/modifyMessage]]
    *
    * @param threadId
    *   The ID of the thread to which this message belongs.
    *
    * @param messageId
    *   The ID of the message to modify.
    */
  def modifyMessage(threadId: String, messageId: String, metadata: Map[String, String]): Request[Either[OpenAIException, MessageData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.threadMessage(threadId, messageId))
      .body(metadata)
      .response(asJsonSnake[MessageData])

  /** Create an assistant with a model and instructions.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/createAssistant]]
    *
    * @param createAssistantBody
    *   Create completion request body.
    */
  def createAssistant(createAssistantBody: CreateAssistantBody): Request[Either[OpenAIException, AssistantData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.Assistants)
      .body(createAssistantBody)
      .response(asJsonSnake[AssistantData])

  /** Create an assistant file by attaching a File to an assistant.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/createAssistantFile]]
    *
    * @param assistantId
    *   The ID of the assistant for which to create a File.
    *
    * @param fileId
    *   A File ID (with purpose="assistants") that the assistant should use. Useful for tools like retrieval and code_interpreter that can
    *   access files..
    */
  def createAssistantFile(assistantId: String, fileId: String): Request[Either[OpenAIException, AssistantFileData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.assistantFiles(assistantId))
      .body(CreateAssistantFileBody(fileId))
      .response(asJsonSnake[AssistantFileData])

  /** Returns a list of assistants.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/listAssistants]]
    */
  def listAssistants(
      queryParameters: QueryParameters = QueryParameters.empty
  ): Request[Either[OpenAIException, ListAssistantsResponse]] = {
    val uri = openAIUris.Assistants
      .withParams(queryParameters.toMap)

    betaOpenAIAuthRequest
      .get(uri)
      .response(asJsonSnake[ListAssistantsResponse])
  }

  /** Returns a list of assistant files.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/listAssistantFiles]]
    *
    * @param assistantId
    *   The ID of the assistant the file belongs to.
    */
  def listAssistantFiles(
      assistantId: String,
      queryParameters: QueryParameters = QueryParameters.empty
  ): Request[Either[OpenAIException, ListAssistantFilesResponse]] = {
    val uri = openAIUris
      .assistantFiles(assistantId)
      .withParams(queryParameters.toMap)

    betaOpenAIAuthRequest
      .get(uri)
      .response(asJsonSnake[ListAssistantFilesResponse])
  }

  /** Retrieves an assistant.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/getAssistant]]
    *
    * @param assistantId
    *   The ID of the assistant to retrieve.
    */
  def retrieveAssistant(assistantId: String): Request[Either[OpenAIException, AssistantData]] =
    betaOpenAIAuthRequest
      .get(openAIUris.assistant(assistantId))
      .response(asJsonSnake[AssistantData])

  /** Retrieves an AssistantFile.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/getAssistantFile]]
    *
    * @param assistantId
    *   The ID of the assistant who the file belongs to.
    *
    * @param fileId
    *   The ID of the file we're getting.
    */
  def retrieveAssistantFile(assistantId: String, fileId: String): Request[Either[OpenAIException, AssistantFileData]] =
    betaOpenAIAuthRequest
      .get(openAIUris.assistantFile(assistantId, fileId))
      .response(asJsonSnake[AssistantFileData])

  /** Modifies an assistant.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/modifyAssistant]]
    *
    * @param assistantId
    *   The ID of the assistant to modify.
    *
    * @param modifyAssistantBody
    *   Modify assistant request body.
    */
  def modifyAssistant(assistantId: String, modifyAssistantBody: ModifyAssistantBody): Request[Either[OpenAIException, AssistantData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.assistant(assistantId))
      .body(modifyAssistantBody)
      .response(asJsonSnake[AssistantData])

  /** Delete an assistant.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/deleteAssistant]]
    *
    * @param assistantId
    *   The ID of the assistant to delete.
    */
  def deleteAssistant(assistantId: String): Request[Either[OpenAIException, DeleteAssistantResponse]] =
    betaOpenAIAuthRequest
      .delete(openAIUris.assistant(assistantId))
      .response(asJsonSnake[DeleteAssistantResponse])

  /** Delete an assistant file.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/deleteAssistantFile]]
    *
    * @param assistantId
    *   The ID of the assistant that the file belongs to.
    *
    * @param fileId
    *   The ID of the file to delete.
    */
  def deleteAssistantFile(assistantId: String, fileId: String): Request[Either[OpenAIException, DeleteAssistantFileResponse]] =
    betaOpenAIAuthRequest
      .delete(openAIUris.assistantFile(assistantId, fileId))
      .response(asJsonSnake[DeleteAssistantFileResponse])

  /** Create a run.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/createRun]]
    *
    * @param threadId
    *   The ID of the thread to run.
    * @param createRun
    *   Create run request body.
    */
  def createRun(threadId: String, createRun: CreateRun): Request[Either[OpenAIException, RunData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.threadRuns(threadId))
      .body(createRun)
      .response(asJsonSnake[RunData])

  /** Create a thread and run it in one request.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/createThreadAndRun]]
    *
    * @param createThreadAndRun
    *   Create thread and run request body.
    */
  def createThreadAndRun(createThreadAndRun: CreateThreadAndRun): Request[Either[OpenAIException, RunData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.ThreadsRuns)
      .body(createThreadAndRun)
      .response(asJsonSnake[RunData])

  /** Returns a list of runs belonging to a thread..
    *
    * [[https://platform.openai.com/docs/api-reference/runs/listRuns]]
    *
    * @param threadId
    *   The ID of the thread the run belongs to.
    */
  def listRuns(threadId: String): Request[Either[OpenAIException, ListRunsResponse]] =
    betaOpenAIAuthRequest
      .get(openAIUris.threadRuns(threadId))
      .response(asJsonSnake[ListRunsResponse])

  /** Returns a list of run steps belonging to a run.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/listRunSteps]]
    *
    * @param threadId
    *   The ID of the thread the run and run steps belong to.
    *
    * @param runId
    *   The ID of the run the run steps belong to.
    */
  def listRunSteps(
      threadId: String,
      runId: String,
      queryParameters: QueryParameters = QueryParameters.empty
  ): Request[Either[OpenAIException, ListRunStepsResponse]] = {
    val uri = openAIUris
      .threadRunSteps(threadId, runId)
      .withParams(queryParameters.toMap)

    betaOpenAIAuthRequest
      .get(uri)
      .response(asJsonSnake[ListRunStepsResponse])
  }

  /** Retrieves a run.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/getRun]]
    *
    * @param threadId
    *   The ID of the thread that was run.
    *
    * @param runId
    *   The ID of the run to retrieve.
    */
  def retrieveRun(threadId: String, runId: String): Request[Either[OpenAIException, RunData]] =
    betaOpenAIAuthRequest
      .get(openAIUris.threadRun(threadId, runId))
      .response(asJsonSnake[RunData])

  /** Retrieves a run step.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/getRunStep]]
    *
    * @param threadId
    *   The ID of the thread to which the run and run step belongs.
    *
    * @param runId
    *   The ID of the run to which the run step belongs.
    *
    * @param stepId
    *   The ID of the run step to retrieve.
    */
  def retrieveRunStep(threadId: String, runId: String, stepId: String): Request[Either[OpenAIException, RunStepData]] =
    betaOpenAIAuthRequest
      .get(openAIUris.threadRunStep(threadId, runId, stepId))
      .response(asJsonSnake[RunStepData])

  /** Modifies a run.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/modifyRun]]
    *
    * @param threadId
    *   The ID of the thread that was run.
    *
    * @param runId
    *   The ID of the run to modify.
    */
  def modifyRun(threadId: String, runId: String, metadata: Map[String, String]): Request[Either[OpenAIException, RunData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.threadRun(threadId, runId))
      .body(ModifyRun(metadata))
      .response(asJsonSnake[RunData])

  /** When a run has the status: "requires_action" and required_action.type is submit_tool_outputs, this endpoint can be used to submit the
    * outputs from the tool calls once they're all completed. All outputs must be submitted in a single request.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/submitToolOutputs]]
    *
    * @param threadId
    *   The ID of the thread to which this run belongs.
    * @param runId
    *   The ID of the run that requires the tool output submission.
    * @param toolOutputs
    *   A list of tools for which the outputs are being submitted.
    */
  def submitToolOutputs(threadId: String, runId: String, toolOutputs: Seq[ToolOutput]): Request[Either[OpenAIException, RunData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.threadRunSubmitToolOutputs(threadId, runId))
      .body(SubmitToolOutputsToRun(toolOutputs))
      .response(asJsonSnake[RunData])
//
  /** Cancels a run that is in_progress.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/cancelRun]]
    *
    * @param threadId
    *   The ID of the thread to which this run belongs.
    *
    * @param runId
    *   The ID of the run to cancel.
    */
  def cancelRun(threadId: String, runId: String): Request[Either[OpenAIException, RunData]] =
    betaOpenAIAuthRequest
      .post(openAIUris.threadRunCancel(threadId, runId))
      .response(asJsonSnake[RunData])

  protected val openAIAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)

  protected val betaOpenAIAuthRequest: PartialRequest[Either[String, String]] =
    openAIAuthRequest.withHeaders(openAIAuthRequest.headers :+ Header("OpenAI-Beta", "assistants=v1"))

}

private class OpenAIUris(val BaseUri: Uri) {
  private val ImageBase: Uri = uri"$BaseUri/images"
  private val AudioBase: Uri = uri"$BaseUri/audio/"

  val ChatCompletions: Uri = uri"$BaseUri/chat/completions"
  val Completions: Uri = uri"$BaseUri/completions"
  val CreateImage: Uri = ImageBase.addPath("generations")
  val Edits: Uri = uri"$BaseUri/edits"
  val Embeddings: Uri = uri"$BaseUri/embeddings"
  val EditImage: Uri = ImageBase.addPath("edits")
  val Files: Uri = uri"$BaseUri/files"
  val FineTunes: Uri = uri"$BaseUri/fine-tunes"
  val Models: Uri = uri"$BaseUri/models"
  val Moderations: Uri = uri"$BaseUri/moderations"
  val Transcriptions: Uri = AudioBase.addPath("transcriptions")
  val Translations: Uri = AudioBase.addPath("translations")
  val VariationsImage: Uri = ImageBase.addPath("variations")

  val Assistants: Uri = uri"$BaseUri/assistants"
  val Threads: Uri = uri"$BaseUri/threads"
  val ThreadsRuns: Uri = uri"$BaseUri/threads/runs"

  def cancelFineTune(fineTuneId: String): Uri = FineTunes.addPath(fineTuneId, "cancel")
  def file(fileId: String): Uri = Files.addPath(fileId)
  def fileContent(fileId: String): Uri = Files.addPath(fileId, "content")
  def fineTuneModel(model: String): Uri = Models.addPath(model)
  def fineTuneEvents(fineTuneId: String): Uri = FineTunes.addPath(fineTuneId, "events")
  def fineTune(fineTuneId: String): Uri = FineTunes.addPath(fineTuneId)
  def model(modelId: String): Uri = Models.addPath(modelId)

  def assistant(assistantId: String): Uri = Assistants.addPath(assistantId)
  def assistantFiles(assistantId: String): Uri = Assistants.addPath(assistantId).addPath("files")
  def assistantFile(assistantId: String, fileId: String): Uri = Assistants.addPath(assistantId).addPath("files").addPath(fileId)

  def thread(threadId: String): Uri = Threads.addPath(threadId)

  def threadMessages(threadId: String): Uri = Threads.addPath(threadId).addPath("messages")
  def threadMessage(threadId: String, messageId: String): Uri = Threads.addPath(threadId).addPath("messages").addPath(messageId)

  def threadMessageFiles(threadId: String, messageId: String): Uri =
    Threads.addPath(threadId).addPath("messages", messageId, "files")
  def threadMessageFile(threadId: String, messageId: String, fileId: String): Uri =
    Threads.addPath(threadId).addPath("messages", messageId, "files", fileId)

  def threadRuns(threadId: String): Uri = Threads.addPath(threadId, "runs")
  def threadRun(threadId: String, runId: String): Uri = Threads.addPath(threadId, "runs", runId)

  def threadRunSteps(threadId: String, runId: String): Uri =
    Threads.addPath(threadId, "runs", runId, "steps")
  def threadRunStep(threadId: String, runId: String, stepId: String): Uri =
    Threads.addPath(threadId, "runs", runId, "steps", stepId)
  def threadRunCancel(threadId: String, runId: String): Uri =
    Threads.addPath(threadId, "runs", runId, "cancel")

  def threadRunSubmitToolOutputs(threadId: String, runId: String): Uri =
    Threads.addPath(threadId, "runs", runId, "submit_tool_outputs")

}

object OpenAIUris {
  val OpenAIBaseUri: Uri = uri"https://api.openai.com/v1"
}
