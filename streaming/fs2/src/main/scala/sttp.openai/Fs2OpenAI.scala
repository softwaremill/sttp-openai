package sttp.openai.streaming.fs2

import fs2.{Pipe, RaiseThrowable, Stream}
import sttp.openai.OpenAI
import sttp.capabilities.fs2.Fs2Streams
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.client4.StreamRequest
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.client4.impl.fs2.Fs2ServerSentEvents
import sttp.model.sse.ServerSentEvent
import sttp.openai.json.SttpUpickleApiExtension.deserializeJsonSnake

object Fs2OpenAI {
  import ChatChunkResponse.DoneEventMessage

  implicit class streaming(val client: OpenAI) {

    /** Creates and streams a model response as chunk objects for the given chat conversation defined in chatBody.
      *
      * [[https://platform.openai.com/docs/api-reference/chat/create]]
      *
      * @param chatBody
      *   Chat request body.
      */
    def createStreamedChatCompletion[F[_]: RaiseThrowable](
        body: ChatBody
    ): StreamRequest[Either[OpenAIException, Stream[F, ChatChunkResponse]], Fs2Streams[F]] =
      client
        .createChatCompletion(Fs2Streams[F], body)
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
    _.collect {
      case ServerSentEvent(Some(data), _, _, _) if data != DoneEventMessage =>
        deserializeJsonSnake[ChatChunkResponse].apply(data)
    }
}
