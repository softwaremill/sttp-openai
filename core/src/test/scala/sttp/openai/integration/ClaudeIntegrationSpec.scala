package sttp.openai.integration

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import sttp.openai.ClaudeSyncClient
import sttp.openai.requests.claude.ClaudeRequestBody.{ClaudeMessageRequest, ClaudeModel}
import sttp.openai.requests.completions.chat.message.Content.TextContent
import sttp.openai.requests.completions.chat.message.Message.UserMessage

// Suppress warnings for unused assertions - these are test assertions that verify behavior
//noinspection ScalaUnusedSymbol

/** Integration tests for Claude API that test against the real Anthropic Claude API.
  *
  * These tests are designed to be cost-efficient by using minimal token usage.
  *
  * To run these tests, set the ANTHROPIC_API_KEY environment variable:
  * {{{
  * export ANTHROPIC_API_KEY=your-api-key-here
  * sbt "testOnly *ClaudeIntegrationSpec"
  * }}}
  *
  * If ANTHROPIC_API_KEY is not defined, all tests will be skipped (not failed).
  */
class ClaudeIntegrationSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll with Eventually {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(500, Millis))

  private var clientOpt: Option[ClaudeSyncClient] = None
  private val maybeApiKey: Option[String] = sys.env.get("ANTHROPIC_API_KEY")

  override def beforeAll(): Unit = {
    super.beforeAll()
    maybeApiKey.foreach { apiKey =>
      clientOpt = Some(ClaudeSyncClient(apiKey))
    }
  }

  override def afterAll(): Unit = {
    clientOpt.foreach(_.close())
    super.afterAll()
  }

  private def withClient(testCode: ClaudeSyncClient => Unit): Unit =
    clientOpt match {
      case Some(client) => testCode(client)
      case None =>
        info("ANTHROPIC_API_KEY not provided - skipping Claude integration test")
        pending
    }

  "Claude API" should "successfully create a basic message" in withClient { client =>
    // given
    val request = ClaudeMessageRequest(
      model = ClaudeModel.Claude3Haiku20240307, // Using Haiku as it's the most cost-efficient
      messages = Seq(UserMessage(TextContent("Hi"))), // Minimal message to reduce costs
      maxTokens = 10 // Minimal token limit to reduce costs
    )

    // when
    val response = client.createMessage(request)

    // then
    response.id should not be empty
    response.`type` shouldBe "message"
    response.role shouldBe "assistant"
    response.content should not be empty
    response.content.head.`type` shouldBe "text"
    response.content.head.text should not be empty
    response.model should include("claude")
    response.usage.promptTokens should be > 0
    response.usage.completionTokens should be > 0
    response.usage.totalTokens shouldBe (response.usage.promptTokens + response.usage.completionTokens)
  }

  it should "handle different Claude models" in withClient { client =>
    // given
    val request = ClaudeMessageRequest(
      model = ClaudeModel.Claude35Sonnet20241022,
      messages = Seq(UserMessage(TextContent("Hi"))),
      maxTokens = 10
    )

    // when
    val response = client.createMessage(request)

    // then
    response.model should include("claude-3-5-sonnet")
    response.content should not be empty
  }

  it should "handle temperature parameter" in withClient { client =>
    // given
    val request = ClaudeMessageRequest(
      model = ClaudeModel.Claude3Haiku20240307,
      messages = Seq(UserMessage(TextContent("Hi"))),
      maxTokens = 10,
      temperature = Some(0.1)
    )

    // when
    val response = client.createMessage(request)

    // then
    response.content should not be empty
  }

  it should "handle system prompt" in withClient { client =>
    // given
    val request = ClaudeMessageRequest(
      model = ClaudeModel.Claude3Haiku20240307,
      messages = Seq(UserMessage(TextContent("Hi"))),
      maxTokens = 15,
      system = Some("Be very brief.")
    )

    // when
    val response = client.createMessage(request)

    // then
    response.content should not be empty
    response.usage.promptTokens should be > 5 // System prompt should add tokens
  }
}
