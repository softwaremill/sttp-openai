package sttp.openai.streaming

import _root_.fs2.{Pipe, RaiseThrowable, Stream}
import sttp.capabilities.fs2.Fs2Streams
import sttp.client4.StreamRequest
import sttp.client4.impl.fs2.Fs2ServerSentEvents
import sttp.model.sse.ServerSentEvent
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.json.SttpUpickleApiExtension.deserializeJsonSnake
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody

package object fs2 {
  import ChatChunkResponse.DoneEvent

  implicit class extension(val client: OpenAI) {

    /** Creates and streams a model response as chunk objects for the given chat conversation defined in chatBody.
      *
      * [[https://platform.openai.com/docs/api-reference/chat/create]]
      *
      * @param chatBody
      *   Chat request body.
      */
    def createStreamedChatCompletion[F[_]: RaiseThrowable](
        chatBody: ChatBody
    ): StreamRequest[Either[OpenAIException, Stream[F, ChatChunkResponse]], Fs2Streams[F]] =
      client
        .createChatCompletion(Fs2Streams[F], chatBody)
        .mapResponse(mapEventToResponse[F])
  }

  private def mapEventToResponse[F[_]: RaiseThrowable](
      response: Either[OpenAIException, Stream[F, Byte]]
  ): Either[OpenAIException, Stream[F, ChatChunkResponse]] =
    response.map(
      _.through(Fs2ServerSentEvents.parse)
        .through(deserializeEvent)
        .rethrow
    )

  private def deserializeEvent[F[_]]: Pipe[F, ServerSentEvent, Either[OpenAIException, ChatChunkResponse]] =
    _.takeWhile(_ != DoneEvent)
      .collect { case ServerSentEvent(Some(data), _, _, _) =>
        deserializeJsonSnake[ChatChunkResponse].apply(data)
      }
}
