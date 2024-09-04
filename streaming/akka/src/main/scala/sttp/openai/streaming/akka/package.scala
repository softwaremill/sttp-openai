package sttp.openai.streaming

import _root_.akka.stream.scaladsl.{Flow, Source}
import _root_.akka.util.ByteString
import sttp.capabilities.akka.AkkaStreams
import sttp.client4.StreamRequest
import sttp.model.sse.ServerSentEvent
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.json.SttpUpickleApiExtension.deserializeJsonSnake
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import sttp.client4.akkahttp.AkkaHttpServerSentEvents

package object akka {
  import ChatChunkResponse.DoneEvent

  implicit class extension(val client: OpenAI) {

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
    ): StreamRequest[Either[OpenAIException, Source[ChatChunkResponse, Any]], AkkaStreams] =
      client
        .createChatCompletionAsBinaryStream(AkkaStreams, chatBody)
        .mapResponse(mapEventToResponse)
  }

  private def mapEventToResponse(
      response: Either[OpenAIException, Source[ByteString, Any]]
  ): Either[OpenAIException, Source[ChatChunkResponse, Any]] =
    response.map(
      _.via(AkkaHttpServerSentEvents.parse)
        .via(deserializeEvent)
    )

  private def deserializeEvent: Flow[ServerSentEvent, ChatChunkResponse, Any] =
    Flow[ServerSentEvent]
      .takeWhile(_ != DoneEvent)
      .collect { case ServerSentEvent(Some(data), _, _, _) =>
        deserializeJsonSnake[ChatChunkResponse].apply(data) match {
          case Left(exception) => throw exception
          case Right(value)    => value
        }
      }
}
