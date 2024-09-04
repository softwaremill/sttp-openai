package sttp.openai.streaming.ox

import ox.channels.Source
import ox.either.orThrow
import ox.*
import sttp.client4.DefaultSyncBackend
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.OpenAIExceptions.OpenAIException.DeserializationOpenAIException
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message.*
import sttp.openai.streaming.ox.*

object demo extends OxApp:
  override def run(args: Vector[String])(using Ox, IO): ExitCode =
    // Read your API secret-key from env variables
    val apiKey = System.getenv("openai-key")

    // Create an instance of OpenAISyncClient providing your API secret-key
    val openAI: OpenAI = new OpenAI(apiKey)

    val bodyMessages: Seq[Message] = Seq(
      Message.UserMessage(
        content = Content.TextContent("Hello!")
      )
    )

    val chatRequestBody: ChatBody = ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = bodyMessages
    )

    val backend = useCloseableInScope(DefaultSyncBackend())
    supervised {
      val source = openAI
        .createStreamedChatCompletion(chatRequestBody)
        .send(backend)
        .body
        .orThrow

      source.foreach(el => println(el.orThrow))
    }

    ExitCode.Success
