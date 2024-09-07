//> using dep com.softwaremill.sttp.openai::ox:0.2.2
//> using dep com.softwaremill.sttp.tapir::tapir-netty-server-sync:1.11.2
//> using dep com.softwaremill.sttp.client4::ox:4.0.0-M17
//> using dep com.softwaremill.ox::core:0.3.7
//> using dep ch.qos.logback:logback-classic:1.5.8

// Remember to set the OPENAI_KEY env variable!

package examples

import org.slf4j.{Logger, LoggerFactory}
import ox.*
import ox.channels.{Channel, ChannelClosed}
import ox.either.orThrow
import sttp.client4.{DefaultSyncBackend, SyncBackend}
import sttp.openai.OpenAI
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message.{Content, Message}
import sttp.openai.streaming.ox.*
import sttp.tapir.*
import sttp.tapir.CodecFormat.*
import sttp.tapir.server.netty.sync.{NettySyncServer, OxStreams}

import scala.annotation.tailrec

//

val logger: Logger = LoggerFactory.getLogger("ChatProxy")

// model for sending & receiving chat messages to/from the end-user
case class ChatMessage(message: String)
given Codec[String, ChatMessage, TextPlain] = Codec.string.map(ChatMessage(_))(_.message)

// the description of the endpoint, that will be exposed: GET /chat -> WS(consuming & producing ChatMessage-s)
val chatProxyEndpoint = infallibleEndpoint.get
  .in("chat")
  .out(webSocketBody[ChatMessage, TextPlain, ChatMessage, TextPlain](OxStreams))

def chat(sttpBackend: SyncBackend, openAI: OpenAI)(using IO): OxStreams.Pipe[ChatMessage, ChatMessage] =
  ox ?=> // running within a concurrency scope
    incoming => {
      val outgoing = Channel.bufferedDefault[ChatMessage]

      // incoming - messages sent by the end-user over the web socket
      // outgoing - messages to be sent to the end user over the web socket

      // main processing loop: receives messages from the WS and queries OpenAI with the chat's history
      @tailrec
      def loop(history: Vector[Message]): Unit = incoming.receiveOrDone() match
        case ChannelClosed.Done => outgoing.done() // we won't be sending any more messages to the client
        case nextMessage: ChatMessage =>
          val nextHistory = history :+ Message.UserMessage(content = Content.TextContent(nextMessage.message))

          // querying OpenAI with the entire chat history, as each request is stateless
          val chatRequestBody: ChatBody = ChatBody(
            model = ChatCompletionModel.GPT4oMini,
            messages = nextHistory
          )

          // requesting a streaming completion, so that we can get back to the user as the answer is being generated
          val source = openAI
            .createStreamedChatCompletion(chatRequestBody)
            .send(sttpBackend)
            .body
            .orThrow // there might be an OpenAI HTTP-error

          // a side-channel onto which we'll collect all the responses, to store it in history for subsequent messages
          val gatherResponse = Channel.bufferedDefault[ChatMessage]
          val gatherResponseFork = fork(gatherResponse.toList.map(_.message).mkString) // collecting the response in the background

          // extracting the response increments, sending to the outgoing channel, as well as to the side-channel
          source
            .mapAsView(_.orThrow.choices.head.delta.content)
            .collectAsView { case Some(msg) => ChatMessage(msg) }
            .alsoTo(gatherResponse)
            .pipeTo(outgoing, propagateDone = false)

          val gatheredResponse = gatherResponseFork.join()
          val nextNextHistory = nextHistory :+ Message.AssistantMessage(content = gatheredResponse)

          loop(nextNextHistory)

      // running the processing in the background, so that we can return the outgoing channel to the library ...
      fork(loop(Vector.empty))

      // ... so that the messages can be sent over the WS
      outgoing
    }

object ChatProxy extends OxApp:
  override def run(args: Vector[String])(using Ox, IO): ExitCode =
    val openAI = new OpenAI(System.getenv("OPENAI_KEY"))
    val sttpBackend = useCloseableInScope(DefaultSyncBackend())
    val chatProxyServerEndpoint = chatProxyEndpoint.handleSuccess(_ => chat(sttpBackend, openAI))
    val binding = NettySyncServer().addEndpoint(chatProxyServerEndpoint).start()
    logger.info(s"Server started at ${binding.hostName}:${binding.port}")
    never
