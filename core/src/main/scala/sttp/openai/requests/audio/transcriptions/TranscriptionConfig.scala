package sttp.openai.requests.audio.transcriptions

import sttp.openai.requests.audio.RecognitionModel
import sttp.openai.requests.images.ResponseFormat

import java.io.File
import java.nio.file.Paths

/** @param language
  *   The language of the input audio. Supplying the input language in ISO-639-1 format will improve accuracy and latency. All supported
  *   languages can be found [[https://github.com/openai/whisper#available-models-and-languages]]
  * @example
  *   language {{{language = Some("pl")}}}
  */
/*
todo: scaladocs
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
 */
case class TranscriptionConfig(
    file: File,
    model: RecognitionModel,
    prompt: Option[String] = None,
    responseFormat: Option[ResponseFormat] = None,
    temperature: Option[Float] = None,
    language: Option[String] = None
)

object TranscriptionConfig {
  def createTranscriptionConfigWithSystemPaths(
      systemPathImage: String,
      model: RecognitionModel,
      prompt: Option[String] = None,
      responseFormat: Option[ResponseFormat] = None,
      temperature: Option[Float] = None,
      language: Option[String] = None
  ): TranscriptionConfig =
    TranscriptionConfig(
      Paths.get(systemPathImage).toFile,
      model,
      prompt,
      responseFormat,
      temperature,
      language
    )
}
