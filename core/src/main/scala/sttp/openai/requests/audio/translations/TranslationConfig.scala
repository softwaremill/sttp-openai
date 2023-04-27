package sttp.openai.requests.audio.translations

import sttp.openai.requests.audio.RecognitionModel
import sttp.openai.requests.images.ResponseFormat

import java.io.File
import java.nio.file.Paths

/** @param file
  *   The audio file to translate, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
  * @param model
  *   ID of the model to use. Only whisper-1 is currently available.
  * @param prompt
  *   An optional text to guide the model's style or continue a previous audio segment. The prompt should be in English.
  * @param responseFormat
  *   The format of the transcript output, in one of these options: json, text, srt, verbose_json, or vtt.
  * @param temperature
  *   The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will
  *   make it more focused and deterministic. If set to 0, the model will use log probability to automatically increase the temperature
  *   until certain thresholds are hit.
  */
case class TranslationConfig(
    file: File,
    model: RecognitionModel,
    prompt: Option[String] = None,
    responseFormat: Option[ResponseFormat] = None,
    temperature: Option[Float] = None
)

object TranslationConfig {
  def createTranslationConfigWithSystemPaths(
      systemPath: String,
      model: RecognitionModel,
      prompt: Option[String] = None,
      responseFormat: Option[ResponseFormat] = None,
      temperature: Option[Float] = None
  ): TranslationConfig =
    TranslationConfig(
      Paths.get(systemPath).toFile,
      model,
      prompt,
      responseFormat,
      temperature
    )
}
