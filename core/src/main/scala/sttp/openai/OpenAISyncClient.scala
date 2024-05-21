package sttp.openai

import sttp.client4.{DefaultSyncBackend, Request, SyncBackend}
import sttp.model.Uri
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.requests.assistants.AssistantsRequestBody.{CreateAssistantBody, ModifyAssistantBody}
import sttp.openai.requests.assistants.AssistantsResponseData.{AssistantData, DeleteAssistantResponse, ListAssistantsResponse}
import sttp.openai.requests.audio.AudioResponseData.AudioResponse
import sttp.openai.requests.audio.RecognitionModel
import sttp.openai.requests.audio.transcriptions.TranscriptionConfig
import sttp.openai.requests.audio.translations.TranslationConfig
import sttp.openai.requests.completions.CompletionsRequestBody.CompletionsBody
import sttp.openai.requests.completions.CompletionsResponseData.CompletionsResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.edit.EditRequestBody.EditBody
import sttp.openai.requests.completions.edit.EditRequestResponseData.EditResponse
import sttp.openai.requests.embeddings.EmbeddingsRequestBody.EmbeddingsBody
import sttp.openai.requests.embeddings.EmbeddingsResponseBody.EmbeddingResponse
import sttp.openai.requests.files.FilesResponseData.{DeletedFileData, FileData, FilesResponse}
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
import sttp.openai.requests.threads.QueryParameters
import sttp.openai.requests.threads.ThreadsRequestBody.CreateThreadBody
import sttp.openai.requests.threads.ThreadsResponseData.{DeleteThreadResponse, ThreadData}
import sttp.openai.requests.threads.messages.ThreadMessagesRequestBody.CreateMessage
import sttp.openai.requests.threads.messages.ThreadMessagesResponseData.{ListMessagesResponse, MessageData}
import sttp.openai.requests.threads.runs.ThreadRunsRequestBody.{CreateRun, CreateThreadAndRun, ToolOutput}
import sttp.openai.requests.threads.runs.ThreadRunsResponseData.{ListRunStepsResponse, ListRunsResponse, RunData, RunStepData}
import sttp.openai.requests.vectorstore.VectorStoreRequestBody.{CreateVectorStoreBody, ModifyVectorStoreBody}
import sttp.openai.requests.vectorstore.VectorStoreResponseData.{DeleteVectorStoreResponse, ListVectorStoresResponse, VectorStore}
import sttp.openai.requests.vectorstore.file.VectorStoreFileRequestBody.{CreateVectorStoreFileBody, ListVectorStoreFilesBody}
import sttp.openai.requests.vectorstore.file.VectorStoreFileResponseData.{
  DeleteVectorStoreFileResponse,
  ListVectorStoreFilesResponse,
  VectorStoreFile
}

import java.io.File

class OpenAISyncClient private (authToken: String, backend: SyncBackend, closeClient: Boolean, baseUri: Uri) {

  private val openAI = new OpenAI(authToken, baseUri)

  /** Lists the currently available models, and provides basic information about each one such as the owner and availability.
    *
    * [[https://platform.openai.com/docs/api-reference/models]]
    */
  def getModels: ModelsResponse =
    sendOrThrow(openAI.getModels)

  /** Retrieves a model instance, providing basic information about the model such as the owner and permissions.
    *
    * [[https://platform.openai.com/docs/api-reference/models/retrieve]]
    *
    * @param modelId
    *   The ID of the model to use for this request.
    */
  def retrieveModel(modelId: String): ModelData =
    sendOrThrow(openAI.retrieveModel(modelId))

  /** Creates a completion for the provided prompt and parameters given in request body.
    *
    * [[https://platform.openai.com/docs/api-reference/completions/create]]
    *
    * @param completionBody
    *   Create completion request body.
    */
  def createCompletion(completionBody: CompletionsBody): CompletionsResponse =
    sendOrThrow(openAI.createCompletion(completionBody))

  /** Creates an image given a prompt in request body.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create]]
    *
    * @param imageCreationBody
    *   Create image request body.
    */
  def createImage(imageCreationBody: ImageCreationBody): ImageResponse =
    sendOrThrow(openAI.createImage(imageCreationBody))

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
  def imageEdits(image: File, prompt: String): ImageResponse =
    sendOrThrow(openAI.imageEdits(image, prompt))

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
  def imageEdits(systemPath: String, prompt: String): ImageResponse =
    sendOrThrow(openAI.imageEdits(systemPath, prompt))

  /** Creates edited or extended images given an original image and a prompt.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-edit]]
    *
    * @param imageEditsConfig
    *   An instance of the case class ImageEditConfig containing the necessary parameters for editing the image.
    */
  def imageEdits(imageEditsConfig: ImageEditsConfig): ImageResponse =
    sendOrThrow(openAI.imageEdits(imageEditsConfig))

  /** Creates a variation of a given image.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-variation]]
    *
    * @param image
    *   The image to use as the basis for the variation.
    *
    * Must be a valid PNG file, less than 4MB, and square.
    */
  def imageVariations(image: File): ImageResponse =
    sendOrThrow(openAI.imageVariations(image))

  /** Creates a variation of a given image.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-variation]]
    *
    * @param systemPath
    *   Path to the image to use as the basis for the variation.
    *
    * Must be a valid PNG file, less than 4MB, and square.
    */
  def imageVariations(systemPath: String): ImageResponse =
    sendOrThrow(openAI.imageVariations(systemPath))

  /** Creates a variation of a given image.
    *
    * [[https://platform.openai.com/docs/api-reference/images/create-variation]]
    *
    * @param imageVariationsConfig
    *   An instance of the case class ImageVariationsConfig containing the necessary parameters for the image variation.
    */
  def imageVariations(imageVariationsConfig: ImageVariationsConfig): ImageResponse =
    sendOrThrow(openAI.imageVariations(imageVariationsConfig))

  /** Creates a new edit for provided request body.
    *
    * [[https://platform.openai.com/docs/api-reference/edits/create]]
    *
    * @param editRequestBody
    *   Edit request body.
    */
  def createEdit(editRequestBody: EditBody): EditResponse =
    sendOrThrow(openAI.createEdit(editRequestBody))

  /** Creates a model response for the given chat conversation defined in chatBody.
    *
    * [[https://platform.openai.com/docs/api-reference/chat/create]]
    *
    * @param chatBody
    *   Chat request body.
    */
  def createChatCompletion(chatBody: ChatBody): ChatResponse =
    sendOrThrow(openAI.createChatCompletion(chatBody))

  /** Returns a list of files that belong to the user's organization.
    *
    * [[https://platform.openai.com/docs/api-reference/files]]
    */
  def getFiles: FilesResponse =
    sendOrThrow(openAI.getFiles)

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
  def uploadFile(file: File, purpose: String): FileData =
    sendOrThrow(openAI.uploadFile(file, purpose))

  /** Upload a file that contains document(s) to be used across various endpoints/features. Currently, the size of all the files uploaded by
    * one organization can be up to 1 GB. Please contact OpenAI if you need to increase the storage limit.
    *
    * [[https://platform.openai.com/docs/api-reference/files/upload]]
    *
    * @param file
    *   JSON Lines file to be uploaded and the purpose is set to "fine-tune", each line is a JSON record with "prompt" and "completion"
    *   fields representing your [[https://platform.openai.com/docs/guides/fine-tuning/prepare-training-data training examples]].
    */
  def uploadFile(file: File): FileData =
    sendOrThrow(openAI.uploadFile(file))

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
  def uploadFile(systemPath: String, purpose: String): FileData =
    sendOrThrow(openAI.uploadFile(systemPath, purpose))

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
  def uploadFile(systemPath: String): FileData =
    sendOrThrow(openAI.uploadFile(systemPath))

  /** Delete a file.
    *
    * [[https://platform.openai.com/docs/api-reference/files/delete]]
    *
    * @param fileId
    *   The ID of the file to use for this request.
    */
  def deleteFile(fileId: String): DeletedFileData =
    sendOrThrow(openAI.deleteFile(fileId))

  /** Returns information about a specific file.
    *
    * [[https://platform.openai.com/docs/api-reference/files/retrieve]]
    *
    * @param fileId
    *   The ID of the file to use for this request.
    */
  def retrieveFile(fileId: String): FileData =
    sendOrThrow(openAI.retrieveFile(fileId))

  /** Returns the contents of the specified file.
    *
    * [[https://platform.openai.com/docs/api-reference/files/retrieve-content]]
    *
    * @param fileId
    *   The ID of the file.
    */
  def retrieveFileContent(fileId: String): String =
    sendOrThrow(openAI.retrieveFileContent(fileId))

  /** Translates audio into English text.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param file
    *   The audio file to translate, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranslation(file: File, model: RecognitionModel): AudioResponse =
    sendOrThrow(openAI.createTranslation(file, model))

  /** Translates audio into English text.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param systemPath
    *   The audio systemPath to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranslation(systemPath: String, model: RecognitionModel): AudioResponse =
    sendOrThrow(openAI.createTranslation(systemPath, model))

  /** Translates audio into English text.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param translationConfig
    *   An instance of the case class TranslationConfig containing the necessary parameters for the audio translation.
    */
  def createTranslation(translationConfig: TranslationConfig): AudioResponse =
    sendOrThrow(openAI.createTranslation(translationConfig))

  /** Classifies if text violates OpenAI's Content Policy.
    *
    * [[https://platform.openai.com/docs/api-reference/moderations/create]]
    *
    * @param moderationsBody
    *   Moderation request body.
    */
  def createModeration(moderationsBody: ModerationsBody): ModerationData =
    sendOrThrow(openAI.createModeration(moderationsBody))

  /** Transcribes audio into the input language.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param file
    *   The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranscription(file: File, model: RecognitionModel): AudioResponse =
    sendOrThrow(openAI.createTranscription(file, model))

  /** Transcribes audio into the input language.
    *
    * [[https://platform.openai.com/docs/api-reference/audio/create]]
    *
    * @param systemPath
    *   The audio systemPath to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   ID of the model to use. Only whisper-1 is currently available.
    */
  def createTranscription(systemPath: String, model: RecognitionModel): AudioResponse =
    sendOrThrow(openAI.createTranscription(systemPath, model))

  /** Transcribes audio into the input language.
    *
    * @param transcriptionConfig
    *   An instance of the case class TranscriptionConfig containing the necessary parameters for the audio transcription
    * @return
    *   An url to edited image.
    */
  def createTranscription(transcriptionConfig: TranscriptionConfig): AudioResponse =
    sendOrThrow(openAI.createTranscription(transcriptionConfig))

  /** Creates a job that fine-tunes a specified model from a given dataset.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/create]]
    *
    * @param fineTunesRequestBody
    *   Request body that will be used to create a fine-tune.
    */
  def createFineTune(fineTunesRequestBody: FineTunesRequestBody): FineTuneResponse =
    sendOrThrow(openAI.createFineTune(fineTunesRequestBody))

  /** List of your organization's fine-tuning jobs.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/list]]
    */
  def getFineTunes: GetFineTunesResponse =
    sendOrThrow(openAI.getFineTunes)

  /** Immediately cancel a fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/cancel]]
    *
    * @param fineTuneId
    *   The ID of the fine-tune job to cancel.
    */
  def cancelFineTune(fineTuneId: String): FineTuneResponse =
    sendOrThrow(openAI.cancelFineTune(fineTuneId))

  /** Gets info about the fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/embeddings/create]]
    *
    * @param embeddingsBody
    *   Embeddings request body.
    */
  def createEmbeddings(embeddingsBody: EmbeddingsBody): EmbeddingResponse =
    sendOrThrow(openAI.createEmbeddings(embeddingsBody))

  /** Gets info about the fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/retrieve]]
    *
    * @param fineTuneId
    *   The ID of the fine-tune job.
    */
  def retrieveFineTune(fineTuneId: String): FineTuneResponse =
    sendOrThrow(openAI.retrieveFineTune(fineTuneId))

  /** Delete a fine-tuned model. You must have the Owner role in your organization.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/delete-model]]
    *
    * @param model
    *   The model to delete.
    */
  def deleteFineTuneModel(model: String): DeleteFineTuneModelResponse =
    sendOrThrow(openAI.deleteFineTuneModel(model))

  /** Get fine-grained status updates for a fine-tune job.
    *
    * [[https://platform.openai.com/docs/api-reference/fine-tunes/events]]
    *
    * @param fineTuneId
    *   The ID of the fine-tune job to get events for.
    */
  def getFineTuneEvents(fineTuneId: String): FineTuneEventsResponse =
    sendOrThrow(openAI.getFineTuneEvents(fineTuneId))

  /** Create a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/createThread]]
    *
    * @param createThreadBody
    *   Create completion request body.
    */
  def createThread(createThreadBody: CreateThreadBody): ThreadData =
    sendOrThrow(openAI.createThread(createThreadBody))

  /** Retrieves a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/getThread]]
    *
    * @param threadId
    *   The ID of the thread to retrieve.
    */
  def retrieveThread(threadId: String): ThreadData =
    sendOrThrow(openAI.retrieveThread(threadId))

  /** Modifies a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/modifyThread]]
    *
    * @param threadId
    *   The ID of the thread to modify. Only the metadata can be modified.
    */
  def modifyThread(threadId: String, metadata: Map[String, String]): ThreadData =
    sendOrThrow(openAI.modifyThread(threadId, metadata))

  /** Delete a thread.
    *
    * [[https://platform.openai.com/docs/api-reference/threads/deleteThread]]
    *
    * @param threadId
    *   The ID of the thread to delete.
    */
  def deleteThread(threadId: String): DeleteThreadResponse =
    sendOrThrow(openAI.deleteThread(threadId))

  /** Create a message.
    *
    * [[https://platform.openai.com/docs/api-reference/messages/createMessage]]
    *
    * @param threadId
    *   The ID of the thread to create a message for.
    */
  def createThreadMessage(threadId: String, message: CreateMessage): MessageData =
    sendOrThrow(openAI.createThreadMessage(threadId, message))

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
  ): ListMessagesResponse =
    sendOrThrow(openAI.listThreadMessages(threadId, queryParameters))

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
  ): MessageData =
    sendOrThrow(openAI.retrieveThreadMessage(threadId, messageId))

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
  def modifyMessage(threadId: String, messageId: String, metadata: Map[String, String]): MessageData =
    sendOrThrow(openAI.modifyMessage(threadId, messageId, metadata))

  /** Create an assistant with a model and instructions.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/createAssistant]]
    *
    * @param createAssistantBody
    *   Create completion request body.
    */
  def createAssistant(createAssistantBody: CreateAssistantBody): AssistantData =
    sendOrThrow(openAI.createAssistant(createAssistantBody))

  /** Returns a list of assistants.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/listAssistants]]
    */
  def listAssistants(
      queryParameters: QueryParameters = QueryParameters.empty
  ): ListAssistantsResponse =
    sendOrThrow(openAI.listAssistants(queryParameters))

  /** Retrieves an assistant.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/getAssistant]]
    *
    * @param assistantId
    *   The ID of the assistant to retrieve.
    */
  def retrieveAssistant(assistantId: String): AssistantData =
    sendOrThrow(openAI.retrieveAssistant(assistantId))

  /** Modifies an assistant.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/modifyAssistant]]
    *
    * @param assistantId
    *   The ID of the assistant to modify.
    *
    * @param modifyAssistantBody
    */
  def modifyAssistant(assistantId: String, modifyAssistantBody: ModifyAssistantBody): AssistantData =
    sendOrThrow(openAI.modifyAssistant(assistantId, modifyAssistantBody))

  /** Delete an assistant.
    *
    * [[https://platform.openai.com/docs/api-reference/assistants/deleteAssistant]]
    *
    * @param assistantId
    *   The ID of the assistant to delete.
    */
  def deleteAssistant(assistantId: String): DeleteAssistantResponse =
    sendOrThrow(openAI.deleteAssistant(assistantId))

  /** Create a run.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/createRun]]
    *
    * @param threadId
    *   The ID of the thread to run.
    * @param createRun
    *   Create run request body.
    */
  def createRun(threadId: String, createRun: CreateRun): RunData =
    sendOrThrow(openAI.createRun(threadId, createRun))

  /** Create a thread and run it in one request.
    *
    * [[https://platform.openai.com/docs/api-reference/runs/createThreadAndRun]]
    *
    * @param createThreadAndRun
    *   Create thread and run request body.
    */
  def createThreadAndRun(createThreadAndRun: CreateThreadAndRun): RunData =
    sendOrThrow(openAI.createThreadAndRun(createThreadAndRun))

  /** Returns a list of runs belonging to a thread..
    *
    * [[https://platform.openai.com/docs/api-reference/runs/listRuns]]
    *
    * @param threadId
    *   The ID of the thread the run belongs to.
    */
  def listRuns(threadId: String): ListRunsResponse =
    sendOrThrow(openAI.listRuns(threadId))

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
  ): ListRunStepsResponse =
    sendOrThrow(openAI.listRunSteps(threadId, runId, queryParameters))

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
  def retrieveRun(threadId: String, runId: String): RunData =
    sendOrThrow(openAI.retrieveRun(threadId, runId))

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
  def retrieveRunStep(threadId: String, runId: String, stepId: String): RunStepData =
    sendOrThrow(openAI.retrieveRunStep(threadId, runId, stepId))

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
  def modifyRun(threadId: String, runId: String, metadata: Map[String, String]): RunData =
    sendOrThrow(openAI.modifyRun(threadId, runId, metadata))

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
  def submitToolOutputs(threadId: String, runId: String, toolOutputs: Seq[ToolOutput]): RunData =
    sendOrThrow(openAI.submitToolOutputs(threadId, runId, toolOutputs))
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
  def cancelRun(threadId: String, runId: String): RunData =
    sendOrThrow(openAI.cancelRun(threadId, runId))

  /** Creates vector store
   *
   * @param createVectorStoreBody
   *   Options for new vector store
   * @return
   *   Newly created vector store or exception
   */
  def createVectorStore(createVectorStoreBody: CreateVectorStoreBody): VectorStore =
    sendOrThrow(openAI.createVectorStore(createVectorStoreBody))

  /** Lists vector store
   *
   * @param queryParameters
   *   Search params
   * @return
   *   List of vector stores matching criteria or exception
   */
  def listVectorStores(
      queryParameters: QueryParameters = QueryParameters.empty
  ): ListVectorStoresResponse =
    sendOrThrow(openAI.listVectorStores(queryParameters))

  /** Retrieves vector store by id
   *
   * @param vectorStoreId
   *   Id of vector store
   * @return
   *   Vector store object or exception
   */
  def retrieveVectorStore(vectorStoreId: String): VectorStore =
    sendOrThrow(openAI.retrieveVectorStore(vectorStoreId))

  /** Modifies vector store
   *
   * @param vectorStoreId
   *   Id of vector store to modify
   * @param modifyVectorStoreBody
   *   New values for store properties
   * @return
   *   Modified vector store object
   */
  def modifyVectorStore(
      vectorStoreId: String,
      modifyVectorStoreBody: ModifyVectorStoreBody
  ): VectorStore =
    sendOrThrow(openAI.modifyVectorStore(vectorStoreId, modifyVectorStoreBody))

  /** Deletes vector store
   *
   * @param vectorStoreId
   *   Id of vector store to be deleted
   * @return
   *   Result of deleted operation
   */
  def deleteVectorStore(vectorStoreId: String): DeleteVectorStoreResponse =
    sendOrThrow(openAI.deleteVectorStore(vectorStoreId))

  /** Creates vector store file
   *
   * @param vectorStoreId
   *   Id of vector store for file
   * @param createVectorStoreFileBody
   *   Properties of file
   * @return
   *   Newly created vector store file
   */
  def createVectorStoreFile(
      vectorStoreId: String,
      createVectorStoreFileBody: CreateVectorStoreFileBody
  ): VectorStoreFile =
    sendOrThrow(openAI.createVectorStoreFile(vectorStoreId, createVectorStoreFileBody))

  /** List files belonging to particular datastore
   *
   * @param vectorStoreId
   *   Id of vector store
   * @param queryParameters
   *   Search params
   * @return
   *   List of vector store files
   */
  def listVectorStoreFiles(
      vectorStoreId: String,
      queryParameters: ListVectorStoreFilesBody = ListVectorStoreFilesBody()
  ): ListVectorStoreFilesResponse =
    sendOrThrow(openAI.listVectorStoreFiles(vectorStoreId, queryParameters))

  /** Retrieves vector store file by id
   *
   * @param vectorStoreId
   *   Id of vector store
   * @param fileId
   *   Id of vector store file
   * @return
   *   Vector store file
   */
  def retrieveVectorStoreFile(vectorStoreId: String, fileId: String): VectorStoreFile =
    sendOrThrow(openAI.retrieveVectorStoreFile(vectorStoreId, fileId))

  /** Deletes vector store file by id
   *
   * @param vectorStoreId
   *   Id of vector store
   * @param fileId
   *   Id of vector store file
   * @return
   *   Result of delete operation
   */
  def deleteVectorStoreFile(vectorStoreId: String, fileId: String): DeleteVectorStoreFileResponse =
    sendOrThrow(openAI.deleteVectorStoreFile(vectorStoreId, fileId))

  /** Closes and releases resources of http client if was not provided explicitly, otherwise works no-op.
    */
  def close(): Unit = if (closeClient) backend.close() else ()

  private def sendOrThrow[A](request: Request[Either[OpenAIException, A]]): A =
    request.send(backend).body match {
      case Right(value)    => value
      case Left(exception) => throw exception
    }
}

object OpenAISyncClient {
  def apply(authToken: String) = new OpenAISyncClient(authToken, DefaultSyncBackend(), true, OpenAIUris.OpenAIBaseUri)
  def apply(authToken: String, backend: SyncBackend) = new OpenAISyncClient(authToken, backend, false, OpenAIUris.OpenAIBaseUri)
  def apply(authToken: String, backend: SyncBackend, baseUrl: Uri) = new OpenAISyncClient(authToken, backend, false, baseUrl)
  def apply(authToken: String, baseUrl: Uri) = new OpenAISyncClient(authToken, DefaultSyncBackend(), true, baseUrl)
}
