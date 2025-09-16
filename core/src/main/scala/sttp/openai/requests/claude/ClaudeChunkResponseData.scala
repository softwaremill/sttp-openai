package sttp.openai.requests.claude

import sttp.openai.json.SnakePickle
import sttp.openai.requests.claude.ClaudeRequestBody.StopReason
import sttp.openai.requests.completions.Usage

object ClaudeChunkResponseData {

  sealed trait ClaudeStreamEvent extends Product with Serializable

  /** Indicates the start of a new message */
  case class MessageStart(message: MessageStartData) extends ClaudeStreamEvent

  /** Contains a content block delta */
  case class ContentBlockStart(index: Int, contentBlock: ContentBlockStartData) extends ClaudeStreamEvent

  /** Contains a delta for a content block */
  case class ContentBlockDelta(index: Int, delta: ContentDelta) extends ClaudeStreamEvent

  /** Indicates the end of a content block */
  case class ContentBlockStop(index: Int) extends ClaudeStreamEvent

  /** Contains a delta for the message */
  case class MessageDelta(delta: MessageDeltaData, usage: Usage) extends ClaudeStreamEvent

  /** Indicates the end of the message */
  case object MessageStop extends ClaudeStreamEvent

  /** Error event */
  case class Error(error: ErrorData) extends ClaudeStreamEvent

  /** Ping event (keep-alive) */
  case object Ping extends ClaudeStreamEvent

  object ClaudeStreamEvent {
    implicit val claudeStreamEventR: SnakePickle.Reader[ClaudeStreamEvent] = SnakePickle
      .reader[ujson.Value]
      .map { json =>
        json("type").str match {
          case "message_start" => MessageStart(SnakePickle.read[MessageStartData](json("message")))
          case "content_block_start" => ContentBlockStart(
              json("index").num.toInt,
              SnakePickle.read[ContentBlockStartData](json("content_block"))
            )
          case "content_block_delta" => ContentBlockDelta(
              json("index").num.toInt,
              SnakePickle.read[ContentDelta](json("delta"))
            )
          case "content_block_stop" => ContentBlockStop(json("index").num.toInt)
          case "message_delta" => MessageDelta(
              SnakePickle.read[MessageDeltaData](json("delta")),
              SnakePickle.read[Usage](json("usage"))
            )
          case "message_stop" => MessageStop
          case "error"        => Error(SnakePickle.read[ErrorData](json("error")))
          case "ping"         => Ping
          case other          => throw new IllegalArgumentException(s"Unknown stream event type: $other")
        }
      }
  }

  case class MessageStartData(
      id: String,
      `type`: String,
      role: String,
      content: Seq[ujson.Value],
      model: String,
      stopReason: Option[String],
      stopSequence: Option[String],
      usage: Usage
  )

  object MessageStartData {
    implicit val messageStartDataR: SnakePickle.Reader[MessageStartData] = SnakePickle.macroR[MessageStartData]
  }

  case class ContentBlockStartData(
      `type`: String,
      text: Option[String]
  )

  object ContentBlockStartData {
    implicit val contentBlockStartDataR: SnakePickle.Reader[ContentBlockStartData] = SnakePickle.macroR[ContentBlockStartData]
  }

  case class ContentDelta(
      `type`: String,
      text: Option[String]
  )

  object ContentDelta {
    implicit val contentDeltaR: SnakePickle.Reader[ContentDelta] = SnakePickle.macroR[ContentDelta]
  }

  case class MessageDeltaData(
      stopReason: Option[StopReason],
      stopSequence: Option[String]
  )

  object MessageDeltaData {
    implicit val messageDeltaDataR: SnakePickle.Reader[MessageDeltaData] = SnakePickle.macroR[MessageDeltaData]
  }

  case class ErrorData(
      `type`: String,
      message: String
  )

  object ErrorData {
    implicit val errorDataR: SnakePickle.Reader[ErrorData] = SnakePickle.macroR[ErrorData]
  }

  /** Wrapper for Claude streaming chunk response
    *
    * @param event
    *   The streaming event type and data
    */
  case class ClaudeChunkResponse(event: ClaudeStreamEvent)

  object ClaudeChunkResponse {
    implicit val claudeChunkResponseR: SnakePickle.Reader[ClaudeChunkResponse] =
      SnakePickle.reader[ClaudeStreamEvent].map(ClaudeChunkResponse(_))
  }
}