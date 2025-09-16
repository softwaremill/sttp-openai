package sttp.openai.streaming

import org.apache.pekko.stream.scaladsl.{Flow, Source}
import org.apache.pekko.util.ByteString
import sttp.capabilities.pekko.PekkoStreams
import sttp.client4.StreamRequest
import sttp.client4.pekkohttp.PekkoHttpServerSentEvents
import sttp.model.ResponseMetadata
import sttp.model.sse.ServerSentEvent
import sttp.openai.{Claude, OpenAI}
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.json.SttpUpickleApiExtension.deserializeJsonSnake
import sttp.openai.requests.audio.speech.SpeechRequestBody
import sttp.openai.requests.claude.ClaudeRequestBody.ClaudeMessageRequest
import sttp.openai.requests.claude.ClaudeChunkResponseData.ClaudeChunkResponse
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody

package object pekko {
  import ChatChunkResponse.DoneEvent

  implicit class extension(val client: OpenAI) {

    /** Generates audio from the input text.
      *
      * [[https://platform.openai.com/docs/api-reference/audio/createSpeech]]
      *
      * @param requestBody
      *   Request body that will be used to create a speech.
      *
      * @return
      *   The audio file content.
      */
    def createSpeech(requestBody: SpeechRequestBody): StreamRequest[Either[OpenAIException, Source[ByteString, Any]], PekkoStreams] =
      client.createSpeechAsBinaryStream(PekkoStreams, requestBody)

    /** Creates and streams a model response as chunk objects for the given chat conversation defined in chatBody. The request will complete
      * and the connection close only once the source is fully consumed.
      *
      * [[https://platform.openai.com/docs/api-reference/chat/create]]
      *
      * @param chatBody
      *   Chat request body.
      */
    def createStreamedChatCompletion(
        chatBody: ChatBody
    ): StreamRequest[Either[OpenAIException, Source[ChatChunkResponse, Any]], PekkoStreams] = {
      val request = client
        .createChatCompletionAsBinaryStream(PekkoStreams, chatBody)

      request.response(request.response.mapWithMetadata(mapEventToResponse))
    }
  }

  implicit class claudeExtension(val client: Claude) {

    /** Creates and streams a model response as chunk objects for the given Claude message request. The request will complete and the
      * connection close only once the source is fully consumed.
      *
      * [[https://docs.anthropic.com/en/api/messages-streaming]]
      *
      * @param messageRequest
      *   Claude message request body.
      */
    def createStreamedMessage(
        messageRequest: ClaudeMessageRequest
    ): StreamRequest[Either[OpenAIException, Source[ClaudeChunkResponse, Any]], PekkoStreams] = {
      val request = client.createMessageStream(PekkoStreams, messageRequest)

      request.response(request.response.mapWithMetadata(mapClaudeEventToResponse))
    }
  }

  private def mapEventToResponse(
      response: Either[OpenAIException, Source[ByteString, Any]],
      metadata: ResponseMetadata
  ): Either[OpenAIException, Source[ChatChunkResponse, Any]] =
    response.map(
      _.via(PekkoHttpServerSentEvents.parse)
        .via(deserializeEvent(metadata))
    )

  private def deserializeEvent(metadata: ResponseMetadata): Flow[ServerSentEvent, ChatChunkResponse, Any] =
    Flow[ServerSentEvent]
      .takeWhile(_ != DoneEvent)
      .collect { case ServerSentEvent(Some(data), _, _, _) =>
        deserializeJsonSnake[ChatChunkResponse].apply(data, metadata) match {
          case Left(exception) => throw exception
          case Right(value)    => value
        }
      }

  private def mapClaudeEventToResponse(
      response: Either[OpenAIException, Source[ByteString, Any]],
      metadata: ResponseMetadata
  ): Either[OpenAIException, Source[ClaudeChunkResponse, Any]] =
    response.map(
      _.via(PekkoHttpServerSentEvents.parse)
        .via(deserializeClaudeEvent(metadata))
    )

  private def deserializeClaudeEvent(metadata: ResponseMetadata): Flow[ServerSentEvent, ClaudeChunkResponse, Any] =
    Flow[ServerSentEvent]
      .collect {
        case ServerSentEvent(Some(data), Some(eventType), _, _) if data.nonEmpty && eventType != "ping" =>
          deserializeJsonSnake[ClaudeChunkResponse].apply(data, metadata) match {
            case Left(exception) => throw exception
            case Right(value)    => value
          }
      }
}
