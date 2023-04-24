package sttp.openai.requests.audio.translations

import sttp.openai.requests.audio.Model
import sttp.openai.requests.images.ResponseFormat

import java.io.File
import java.nio.file.Paths

case class TranslationConfig(
    file: File,
    model: Model,
    prompt: Option[String] = None,
    responseFormat: Option[ResponseFormat] = None,
    temperature: Option[Float] = None
)

object TranslationConfig {
  def createTranslationConfigWithSystemPaths(
      systemPath: String,
      model: Model,
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
