package sttp.openai.requests.audio.transcriptions

import sttp.openai.requests.audio.RecognitionModel
import sttp.openai.requests.images.ResponseFormat

import java.io.File
import java.nio.file.Paths

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
