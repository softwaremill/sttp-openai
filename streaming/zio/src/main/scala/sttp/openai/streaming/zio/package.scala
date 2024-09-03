package sttp.openai.streaming

import sttp.client4.StreamRequest
import sttp.model.sse.ServerSentEvent
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.json.SttpUpickleApiExtension.deserializeJsonSnake
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import _root_.zio.stream._
import _root_.zio.ZIO
import sttp.capabilities.zio.ZioStreams
import sttp.client4.impl.zio.ZioServerSentEvents

package object zio {
  import ChatChunkResponse.DoneEvent

  implicit class extension(val client: OpenAI) {

    /** Creates and streams a model response as chunk objects for the given chat conversation defined in chatBody.
      *
      * [[https://platform.openai.com/docs/api-reference/chat/create]]
      *
      * @param chatBody
      *   Chat request body.
      */
    def createStreamedChatCompletion(
        chatBody: ChatBody
    ): StreamRequest[Either[OpenAIException, Stream[Throwable, ChatChunkResponse]], ZioStreams] =
      client
        .createChatCompletionAsBinaryStream(ZioStreams, chatBody)
        .mapResponse(mapEventToResponse)
  }

  private def mapEventToResponse(
      response: Either[OpenAIException, Stream[Throwable, Byte]]
  ): Either[OpenAIException, Stream[Throwable, ChatChunkResponse]] =
    response.map(
      _.viaFunction(ZioServerSentEvents.parse)
        .viaFunction(deserializeEvent)
    )

  private def deserializeEvent: ZioStreams.Pipe[ServerSentEvent, ChatChunkResponse] =
    _.takeWhile(_ != DoneEvent)
      .collectZIO { case ServerSentEvent(Some(data), _, _, _) =>
        ZIO.fromEither(deserializeJsonSnake[ChatChunkResponse].apply(data))
      }
}
