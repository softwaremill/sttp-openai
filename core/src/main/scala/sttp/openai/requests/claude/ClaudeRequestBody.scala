package sttp.openai.requests.claude

import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.message.Message

object ClaudeRequestBody {

  sealed trait ClaudeModel extends Product with Serializable {
    def value: String
  }

  object ClaudeModel {
    case object Claude3Opus20240229 extends ClaudeModel {
      override val value: String = "claude-3-opus-20240229"
    }
    case object Claude3Sonnet20240229 extends ClaudeModel {
      override val value: String = "claude-3-sonnet-20240229"
    }
    case object Claude3Haiku20240307 extends ClaudeModel {
      override val value: String = "claude-3-haiku-20240307"
    }
    case object Claude35Sonnet20241022 extends ClaudeModel {
      override val value: String = "claude-3-5-sonnet-20241022"
    }
    case object Claude35Haiku20241022 extends ClaudeModel {
      override val value: String = "claude-3-5-haiku-20241022"
    }
    case object ClaudeSonnet420250514 extends ClaudeModel {
      override val value: String = "claude-sonnet-4-20250514"
    }

    val values: Set[ClaudeModel] = Set(
      Claude3Opus20240229,
      Claude3Sonnet20240229,
      Claude3Haiku20240307,
      Claude35Sonnet20241022,
      Claude35Haiku20241022,
      ClaudeSonnet420250514
    )

    implicit val claudeModelW: SnakePickle.Writer[ClaudeModel] = SnakePickle.writer[String].comap(_.value)
  }

  sealed trait StopReason extends Product with Serializable {
    def value: String
  }

  object StopReason {
    case object EndTurn extends StopReason {
      override val value: String = "end_turn"
    }
    case object MaxTokens extends StopReason {
      override val value: String = "max_tokens"
    }
    case object StopSequence extends StopReason {
      override val value: String = "stop_sequence"
    }
    case object ToolUse extends StopReason {
      override val value: String = "tool_use"
    }

    implicit val stopReasonR: SnakePickle.Reader[StopReason] = SnakePickle.reader[String].map {
      case "end_turn"      => EndTurn
      case "max_tokens"    => MaxTokens
      case "stop_sequence" => StopSequence
      case "tool_use"      => ToolUse
      case other           => throw new IllegalArgumentException(s"Unknown stop reason: $other")
    }
  }

  /** @param model
    *   The Claude model to use for the completion.
    * @param messages
    *   The messages in the conversation. Must have at least one message.
    * @param maxTokens
    *   The maximum number of tokens to generate before stopping. Required for Claude API.
    * @param system
    *   System prompt to be passed to the model. This is separate from messages in Claude API.
    * @param temperature
    *   Amount of randomness injected into the response. Ranges from 0.0 to 1.0.
    * @param topP
    *   Use nucleus sampling. Ranges from 0.0 to 1.0.
    * @param topK
    *   Only sample from the top K options for each subsequent token. Used to remove "long tail" low probability responses.
    * @param stopSequences
    *   Custom text sequences that will cause the model to stop generating.
    * @param stream
    *   Whether to incrementally stream the response using server-sent events.
    */
  case class ClaudeMessageRequest(
      model: ClaudeModel,
      messages: Seq[Message],
      maxTokens: Int,
      system: Option[String] = None,
      temperature: Option[Double] = None,
      topP: Option[Double] = None,
      topK: Option[Int] = None,
      stopSequences: Option[Seq[String]] = None,
      stream: Option[Boolean] = None
  )

  object ClaudeMessageRequest {
    implicit val claudeMessageRequestW: SnakePickle.Writer[ClaudeMessageRequest] = SnakePickle.macroW[ClaudeMessageRequest]
  }
}
