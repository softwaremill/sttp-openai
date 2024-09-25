package sttp.openai.streaming.ox

import ox.channels.Source
import ox.Ox
import sttp.client4.Request
import sttp.client4.impl.ox.sse.OxServerSentEvents
import sttp.model.sse.ServerSentEvent
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.json.SttpUpickleApiExtension.deserializeJsonSnake
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse.DoneEvent
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody

import java.io.InputStream

extension (client: OpenAI)
  /** Creates and streams a model response as chunk objects for the given chat conversation defined in chatBody.
    *
    * The chunk [[Source]] can be obtained from the response within a concurrency scope (e.g. [[ox.supervised]]), and the [[IO]] capability
    * must be provided. The request will complete and the connection close only once the source is fully consumed.
    *
    * [[https://platform.openai.com/docs/api-reference/chat/create]]
    *
    * @param chatBody
    *   Chat request body.
    */
  def createStreamedChatCompletion(
      chatBody: ChatBody
  ): Request[Either[OpenAIException, Ox ?=> Source[Either[DeserializationOpenAIException, ChatChunkResponse]]]] =
    client
      .createChatCompletionAsInputStream(chatBody)
      .mapResponse(mapEventToResponse)

private def mapEventToResponse(
    response: Either[OpenAIException, InputStream]
): Either[OpenAIException, Ox ?=> Source[Either[DeserializationOpenAIException, ChatChunkResponse]]] =
  response.map(s =>
    OxServerSentEvents
      .parse(s)
      .transform {
        _.takeWhile(_ != DoneEvent)
          .collect { case ServerSentEvent(Some(data), _, _, _) =>
            deserializeJsonSnake[ChatChunkResponse].apply(data)
          }
      }
  )
