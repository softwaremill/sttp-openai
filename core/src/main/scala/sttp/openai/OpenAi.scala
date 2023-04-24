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
import sttp.openai.requests.files.FilesResponseData._
import sttp.openai.requests.images.creation.ImageCreationRequestBody.ImageCreationBody
import sttp.openai.requests.images.edit.ImageEditConfig
import sttp.openai.requests.images.ImageResponseData.ImageResponse
import sttp.openai.requests.images.variations.ImageVariationConfig
import sttp.openai.requests.models.ModelsResponseData.{ModelData, ModelsResponse}
import sttp.openai.requests.audio.AudioResponseData.AudioResponse
import sttp.openai.requests.audio.transcriptions.TranscriptionConfig

import java.io.File
import java.nio.file.Paths

class OpenAi(authToken: String) {

  /** Fetches all available models from [[https://platform.openai.com/docs/api-reference/models]] */
  def getModels: Request[Either[ResponseException[String, Exception], ModelsResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.ModelEndpoint)
      .response(asJsonSnake[ModelsResponse])

  /** @param completionBody
    *   Request body
    *
    * Creates a completion for the provided prompt and parameters given in request body and send it over to
    * [[https://api.openai.com/v1/completions]]
    */
  def createCompletion(completionBody: CompletionsBody): Request[Either[ResponseException[String, Exception], CompletionsResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.CompletionsEndpoint)
      .body(completionBody)
      .response(asJsonSnake[CompletionsResponse])

  /** @param modelId
    *   a Model's Id as String
    *
    * Fetches an available model for given modelId from [[https://platform.openai.com/docs/api-reference/models/{modelId}]]
    */
  def retrieveModel(modelId: String): Request[Either[ResponseException[String, Exception], ModelData]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.retrieveModelEndpoint(modelId))
      .response(asJsonSnake[ModelData])

  /** Fetches all files that belong to the user's organization from [[https://platform.openai.com/docs/api-reference/files]] */
  def getFiles: Request[Either[ResponseException[String, Exception], FilesResponse]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.FilesEndpoint)
      .response(asJsonSnake[FilesResponse])

    /** @param fileId
      *   The ID of the file to use for this request.
      * @return
      *   Information about deleted file.
      */
  def deleteFile(fileId: String): Request[Either[ResponseException[String, Exception], DeletedFileData]] =
    openApiAuthRequest
      .delete(OpenAIEndpoints.deleteFileEndpoint(fileId))
      .response(asJsonSnake[DeletedFileData])

  /** @param imageCreationBody
    *   Create image request body
    *
    * Creates an image given a prompt in request body and send it over to [[https://api.openai.com/v1/images/generations]]
    */
  def createImage(imageCreationBody: ImageCreationBody): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.CreateImageEndpoint)
      .body(imageCreationBody)
      .response(asJsonSnake[ImageResponse])

  /** Creates an edited or extended image given an original image and a prompt
    * @param image
    *   [[java.io.File File]] of the JSON Lines image to be edited. <p> Must be a valid PNG file, less than 4MB, and square. If mask is not
    *   provided, image must have transparency, which will be used as the mask
    * @param prompt
    *   A text description of the desired image(s). The maximum length is 1000 characters.
    * @return
    *   An url to edited image.
    */
  def imageEdit(image: File, prompt: String): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.EditImageEndpoint)
      .multipartBody(
        multipart("prompt", prompt),
        multipartFile("image", image)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates an edited or extended image given an original image and a prompt
    *
    * @param systemPath
    *   [[java.lang.String systemPath]] of the JSON Lines image to be edited. <p> Must be a valid PNG file, less than 4MB, and square. If
    *   mask is not provided, image must have transparency, which will be used as the mask
    * @param prompt
    *   A text description of the desired image(s). The maximum length is 1000 characters.
    * @return
    *   An url to edited image.
    */
  def imageEdit(systemPath: String, prompt: String): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.EditImageEndpoint)
      .multipartBody(
        multipart("prompt", prompt),
        multipartFile("image", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates an edited or extended image given an original image and a prompt
    *
    * @param imageEditConfig
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
  def imageEdit(
      imageEditConfig: ImageEditConfig
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.EditImageEndpoint)
      .multipartBody {
        import imageEditConfig._
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

  /** Creates a variation of a given image
    *
    * @param image
    *   [[java.io.File File]] of the JSON Lines base image. <p> Must be a valid PNG file, less than 4MB, and square.
    * @return
    *   An url to edited image.
    */
  def imageVariation(
      image: File
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.VariationsImageEndpoint)
      .multipartBody(
        multipartFile("image", image)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates a variation of a given image
    *
    * @param systemPath
    *   [[java.lang.String systemPath]] of the JSON Lines base image. <p> Must be a valid PNG file, less than 4MB, and square.
    * @return
    *   An url to edited image.
    */
  def imageVariation(
      systemPath: String
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.VariationsImageEndpoint)
      .multipartBody(
        multipartFile("image", Paths.get(systemPath).toFile)
      )
      .response(asJsonSnake[ImageResponse])

  /** Creates a variation of a given image
    *
    * @param imageVariationConfig
    *   An instance of the case class ImageVariationConfig containing the necessary parameters for the image variation
    *   - image: A file of base image.
    *   - n: An optional integer specifying the number of images to generate.
    *   - size: An optional instance of the Size case class representing the desired size of the output image.
    *   - responseFormat: An optional instance of the ResponseFormat case class representing the desired format of the response.
    *   - user: An optional, unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
    * @return
    *   An url to edited image.
    */
  def imageVariation(
      imageVariationConfig: ImageVariationConfig
  ): Request[Either[ResponseException[String, Exception], ImageResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.VariationsImageEndpoint)
      .multipartBody {
        import imageVariationConfig._
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
    * Creates a new edit for provided request body and send it over to [[https://api.openai.com/v1/chat/completions]]
    */
  def createEdit(editRequestBody: EditBody): Request[Either[ResponseException[String, Exception], EditResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.EditEndpoint)
      .body(editRequestBody)
      .response(asJsonSnake[EditResponse])

  /** @param chatBody
    *   Chat request body
    *
    * Creates a completion for the chat message given in request body and send it over to [[https://api.openai.com/v1/chat/completions]]
    */
  def createChatCompletion(chatBody: ChatBody): Request[Either[ResponseException[String, Exception], ChatResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.ChatEndpoint)
      .body(chatBody)
      .response(asJsonSnake[ChatResponse])

  /** @param fileId
    *   The ID of the file to use for this request.
    * @return
    *   Returns information about a specific file.
    */
  def retrieveFile(fileId: String): Request[Either[ResponseException[String, Exception], FileData]] =
    openApiAuthRequest
      .get(OpenAIEndpoints.retrieveFileEndpoint(fileId))
      .response(asJsonSnake[FileData])

  /** Transcribes audio into the input language
    *
    * @param file
    *   [[java.io.File File]] The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   [[java.lang.String]] ID of the model to use. Only [[whisper-1]] is currently available.
    * @return
    *   Transcription of recorded audio into text.
    */
  def createTranscription(file: File, model: String): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.TranscriptionEndpoint)
      .multipartBody(
        multipartFile("file", file),
        multipart("model", model)
      )
      .response(asJsonSnake[AudioResponse])

  /** Transcribes audio into the input language
    *
    * @param systemPath
    *   [[java.lang.String]] The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    * @param model
    *   [[java.lang.String]] ID of the model to use. Only [[whisper-1]] is currently available.
    * @return
    *   Transcription of recorded audio into text.
    */
  def createTranscription(systemPath: String, model: String): Request[Either[ResponseException[String, Exception], AudioResponse]] =
    openApiAuthRequest
      .post(OpenAIEndpoints.TranscriptionEndpoint)
      .multipartBody(
        multipartFile("file", Paths.get(systemPath).toFile),
        multipart("model", model)
      )
      .response(asJsonSnake[AudioResponse])

  /** Transcribes audio into the input language
    *
    * @param transcriptionConfig
    *   An instance of the case class TranscriptionConfig containing the necessary parameters for the audio transcription
    *   - file: The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
    *   - model: ID of the model to use. Only [[whisper-1]] is currently available.
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
      .post(OpenAIEndpoints.TranscriptionEndpoint)
      .multipartBody {
        import transcriptionConfig._
        Seq(
          Some(multipartFile("file", file)),
          Some(multipart("model", model)),
          prompt.map(multipart("prompt", _)),
          responseFormat.map(format => multipart("response_format", format.value)),
          temperature.map(multipart("temperature", _)),
          language.map(multipart("language", _))
        ).flatten
      }
      .response(asJsonSnake[AudioResponse])

  private val openApiAuthRequest: PartialRequest[Either[String, String]] = basicRequest.auth
    .bearer(authToken)
}

private object OpenAIEndpoints {
  private val ImageEndpointBase: Uri = uri"https://api.openai.com/v1/images"
  private val AudioEndpoint: Uri = uri"https://api.openai.com/v1/audio/"

  val ChatEndpoint: Uri = uri"https://api.openai.com/v1/chat/completions"
  val CompletionsEndpoint: Uri = uri"https://api.openai.com/v1/completions"
  val CreateImageEndpoint: Uri = ImageEndpointBase.addPath("generations")
  val EditEndpoint: Uri = uri"https://api.openai.com/v1/edits"
  val EditImageEndpoint: Uri = ImageEndpointBase.addPath("edits")
  val FilesEndpoint: Uri = uri"https://api.openai.com/v1/files"
  val ModelEndpoint: Uri = uri"https://api.openai.com/v1/models"
  val TranscriptionEndpoint: Uri = AudioEndpoint.addPath("transcriptions")
  val VariationsImageEndpoint: Uri = ImageEndpointBase.addPath("variations")

  def deleteFileEndpoint(fileId: String): Uri = FilesEndpoint.addPath(fileId)
  def retrieveFileEndpoint(fileId: String): Uri = FilesEndpoint.addPath(fileId)
  def retrieveModelEndpoint(modelId: String): Uri = ModelEndpoint.addPath(modelId)
}
