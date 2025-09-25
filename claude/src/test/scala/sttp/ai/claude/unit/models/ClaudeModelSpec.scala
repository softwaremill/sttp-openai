package sttp.ai.claude.unit.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.ai.claude.models.ClaudeModel

class ClaudeModelSpec extends AnyFlatSpec with Matchers {

  "ClaudeModel" should "have correct string values" in {
    ClaudeModel.Claude3_5Sonnet.value shouldBe "claude-3-5-sonnet-20241022"
    ClaudeModel.Claude3_5SonnetLatest.value shouldBe "claude-3-5-sonnet-latest"
    ClaudeModel.Claude3_5Haiku.value shouldBe "claude-3-5-haiku-20241022"
    ClaudeModel.Claude3_5HaikuLatest.value shouldBe "claude-3-5-haiku-latest"
    ClaudeModel.Claude3Opus.value shouldBe "claude-3-opus-20240229"
    ClaudeModel.Claude3Sonnet.value shouldBe "claude-3-sonnet-20240229"
    ClaudeModel.Claude3Haiku.value shouldBe "claude-3-haiku-20240307"
  }

  it should "convert toString correctly" in {
    ClaudeModel.Claude3_5Sonnet.toString shouldBe "claude-3-5-sonnet-20241022"
    ClaudeModel.Claude3Opus.toString shouldBe "claude-3-opus-20240229"
  }

  it should "find models from string values" in {
    ClaudeModel.fromString("claude-3-5-sonnet-20241022") shouldBe Some(ClaudeModel.Claude3_5Sonnet)
    ClaudeModel.fromString("claude-3-opus-20240229") shouldBe Some(ClaudeModel.Claude3Opus)
    ClaudeModel.fromString("invalid-model") shouldBe None
  }

  it should "have all models in values set" in {
    ClaudeModel.values should contain(ClaudeModel.Claude3_5Sonnet)
    ClaudeModel.values should contain(ClaudeModel.Claude3_5SonnetLatest)
    ClaudeModel.values should contain(ClaudeModel.Claude3_5Haiku)
    ClaudeModel.values should contain(ClaudeModel.Claude3_5HaikuLatest)
    ClaudeModel.values should contain(ClaudeModel.Claude3Opus)
    ClaudeModel.values should contain(ClaudeModel.Claude3Sonnet)
    ClaudeModel.values should contain(ClaudeModel.Claude3Haiku)
    ClaudeModel.values should have size 7
  }
}
