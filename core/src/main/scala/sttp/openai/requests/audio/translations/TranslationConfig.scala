package sttp.openai.requests.audio.translations

import sttp.openai.requests.images.ResponseFormat

import java.io.File

case class TranslationConfig(
    file: File,
    model: String,
    prompt: Option[String] = None,
    responseFormat: Option[ResponseFormat] = None,
    temperature: Option[Float] = None
)

object TranslationConfig {
  def createTranslationConfigWithSystemPaths(
      systemPath: String,
      model: String,
      prompt: Option[String] = None,
      responseFormat: Option[ResponseFormat] = None,
      temperature: Option[Float] = None
  ): TranslationConfig =
    TranslationConfig(
      systemPath,
      model,
      prompt,
      responseFormat,
      temperature
    )
}
