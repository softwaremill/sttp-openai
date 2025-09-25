package sttp.ai.claude.unit.config

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.ai.claude.config.ClaudeConfig
import sttp.model.Uri

class ClaudeConfigSpec extends AnyFlatSpec with Matchers {

  "ClaudeConfig" should "create config with minimal parameters" in {
    val config = ClaudeConfig("test-api-key")

    config.apiKey shouldBe "test-api-key"
    config.anthropicVersion shouldBe "2023-06-01"
    config.baseUrl shouldBe Uri.unsafeParse("https://api.anthropic.com")
    config.maxRetries shouldBe 3
    config.organization shouldBe None
  }

  it should "create config with custom anthropic version" in {
    val config = ClaudeConfig("test-api-key", "2024-01-01")

    config.apiKey shouldBe "test-api-key"
    config.anthropicVersion shouldBe "2024-01-01"
  }

  it should "create config with all parameters" in {
    val config = ClaudeConfig(
      apiKey = "test-api-key",
      anthropicVersion = "2024-01-01",
      baseUrl = Uri.unsafeParse("https://custom.api.com"),
      maxRetries = 5,
      organization = Some("test-org")
    )

    config.apiKey shouldBe "test-api-key"
    config.anthropicVersion shouldBe "2024-01-01"
    config.baseUrl shouldBe Uri.unsafeParse("https://custom.api.com")
    config.maxRetries shouldBe 5
    config.organization shouldBe Some("test-org")
  }
}
