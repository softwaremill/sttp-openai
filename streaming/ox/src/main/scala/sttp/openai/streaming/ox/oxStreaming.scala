package sttp.openai.streaming.ox

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
import ox.flow.Flow
import sttp.openai.requests.audio.speech.SpeechRequestBody

extension (client: OpenAI)
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
  def createSpeech(requestBody: SpeechRequestBody): Request[Either[OpenAIException, InputStream]] =
    client.createSpeechAsInputStream(requestBody)

  /** Creates and streams a model response as chunk objects for the given chat conversation defined in chatBody.
    *
    * The request will complete and the connection close only once the returned [[Flow]] is fully consumed.
    *
    * [[https://platform.openai.com/docs/api-reference/chat/create]]
    *
    * @param chatBody
    *   Chat request body.
    */
  def createStreamedChatCompletion(
      chatBody: ChatBody
  ): Request[Either[OpenAIException, Flow[Either[DeserializationOpenAIException, ChatChunkResponse]]]] =
    client
      .createChatCompletionAsInputStream(chatBody)
      .mapResponse(mapEventToResponse)

private def mapEventToResponse(
    response: Either[OpenAIException, InputStream]
): Either[OpenAIException, Flow[Either[DeserializationOpenAIException, ChatChunkResponse]]] =
  response.map(s =>
    OxServerSentEvents
      .parse(s)
      .takeWhile(_ != DoneEvent)
      .collect { case ServerSentEvent(Some(data), _, _, _) =>
        deserializeJsonSnake[ChatChunkResponse].apply(data)
      }
  )
