package sttp.openai.streaming

import _root_.zio.ZIO
import _root_.zio.stream._
import sttp.capabilities.zio.ZioStreams
import sttp.client4.StreamRequest
import sttp.client4.impl.zio.ZioServerSentEvents
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

package object zio {
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
    def createSpeech(requestBody: SpeechRequestBody): StreamRequest[Either[OpenAIException, Stream[Throwable, Byte]], ZioStreams] =
      client.createSpeechAsBinaryStream(ZioStreams, requestBody)

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
    ): StreamRequest[Either[OpenAIException, Stream[Throwable, ChatChunkResponse]], ZioStreams] = {
      val request = client
        .createChatCompletionAsBinaryStream(ZioStreams, chatBody)
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
    ): StreamRequest[Either[OpenAIException, Stream[Throwable, ClaudeChunkResponse]], ZioStreams] = {
      val request = client.createMessageStream(ZioStreams, messageRequest)
      request.response(request.response.mapWithMetadata(mapClaudeEventToResponse))
    }
  }

  private def mapEventToResponse(
      response: Either[OpenAIException, Stream[Throwable, Byte]],
      metadata: ResponseMetadata
  ): Either[OpenAIException, Stream[Throwable, ChatChunkResponse]] =
    response.map(
      _.viaFunction(ZioServerSentEvents.parse)
        .viaFunction(deserializeEvent(metadata))
    )

  private def deserializeEvent(metadata: ResponseMetadata): ZioStreams.Pipe[ServerSentEvent, ChatChunkResponse] =
    _.takeWhile(_ != DoneEvent)
      .collectZIO { case ServerSentEvent(Some(data), _, _, _) =>
        ZIO.fromEither(deserializeJsonSnake[ChatChunkResponse].apply(data, metadata))
      }

  private def mapClaudeEventToResponse(
      response: Either[OpenAIException, Stream[Throwable, Byte]],
      metadata: ResponseMetadata
  ): Either[OpenAIException, Stream[Throwable, ClaudeChunkResponse]] =
    response.map(
      _.viaFunction(ZioServerSentEvents.parse)
        .viaFunction(deserializeClaudeEvent(metadata))
    )

  private def deserializeClaudeEvent(metadata: ResponseMetadata): ZioStreams.Pipe[ServerSentEvent, ClaudeChunkResponse] =
    _.collectZIO {
      case ServerSentEvent(Some(data), Some(eventType), _, _) if data.nonEmpty && eventType != "ping" =>
        ZIO.fromEither(deserializeJsonSnake[ClaudeChunkResponse].apply(data, metadata))
    }
}
