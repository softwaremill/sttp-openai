package sttp.openai.requests.audio.transcriptions

import sttp.openai.requests.audio.RecognitionModel
import sttp.openai.requests.images.ResponseFormat

import java.io.File
import java.nio.file.Paths

/** @param file
  *   The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
  * @param model
  *   ID of the model to use. Only whisper-1 is currently available.
  * @param prompt
  *   An optional text to guide the model's style or continue a previous audio segment. The prompt should match the audio language.
  * @param responseFormat
  *   <<<<<<< HEAD The format of the transcript output, in one of these options: json, text, srt, verbose_json, or vtt.
  * \======= The format of the transcript output, one of `ResponseFormat` >>>>>>> afd137791576c716f2a719db86c01c822e8fa45f
  * @param temperature
  *   The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will
  *   make it more focused and deterministic. If set to 0, the model will use log probability to automatically increase the temperature
  *   until certain thresholds are hit.
  * @param language
  *   The language of the input audio. Supplying the input language in ISO-639-1 format will improve accuracy and latency.
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
