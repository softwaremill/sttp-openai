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
