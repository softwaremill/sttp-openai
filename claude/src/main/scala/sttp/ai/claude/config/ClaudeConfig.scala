package sttp.ai.claude.config

import sttp.model.Uri

import scala.concurrent.duration.{Duration, DurationInt}

case class ClaudeConfig(
    apiKey: String,
    anthropicVersion: String = "2023-06-01",
    baseUrl: Uri = ClaudeConfig.DefaultBaseUrl,
    timeout: Duration = 60.seconds,
    maxRetries: Int = 3,
    organization: Option[String] = None
)

object ClaudeConfig {
  val DefaultBaseUrl: Uri = Uri.unsafeParse("https://api.anthropic.com")

  def fromEnv: ClaudeConfig = {
    val apiKey =
      sys.env.getOrElse("ANTHROPIC_API_KEY", throw new IllegalArgumentException("ANTHROPIC_API_KEY environment variable is required"))
    val anthropicVersion = sys.env.getOrElse("ANTHROPIC_VERSION", "2023-06-01")
    val baseUrl = sys.env.get("ANTHROPIC_BASE_URL").map(Uri.unsafeParse).getOrElse(DefaultBaseUrl)

    ClaudeConfig(
      apiKey = apiKey,
      anthropicVersion = anthropicVersion,
      baseUrl = baseUrl
    )
  }

  def apply(apiKey: String): ClaudeConfig = ClaudeConfig(
    apiKey = apiKey
  )

  def apply(apiKey: String, anthropicVersion: String): ClaudeConfig = ClaudeConfig(
    apiKey = apiKey,
    anthropicVersion = anthropicVersion
  )
}
