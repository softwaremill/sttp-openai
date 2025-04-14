![sttp-openai](https://github.com/softwaremill/sttp-openai/raw/master/banner.jpg)

[![Ideas, suggestions, problems, questions](https://img.shields.io/badge/Discourse-ask%20question-blue)](https://softwaremill.community/c/tapir)
[![CI](https://github.com/softwaremill/sttp-openai/workflows/CI/badge.svg)](https://github.com/softwaremill/sttp-openai/actions?query=workflow%3ACI+branch%3Amaster)

[//]: # ([![Maven Central]&#40;https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp.openai.svg&#41;&#40;https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp.openai&#41;)

sttp is a family of Scala HTTP-related projects, and currently includes:

* [sttp client](https://github.com/softwaremill/sttp): The Scala HTTP client you always wanted!
* [sttp tapir](https://github.com/softwaremill/tapir): Typed API descRiptions
* sttp openai: this project. Non-official Scala client wrapper for OpenAI (and OpenAI-compatible) API. Use the power of ChatGPT inside your code!

## Intro

sttp-openai uses sttp client to describe requests and responses used in OpenAI (and OpenAI-compatible) endpoints.

## Quickstart with sbt

Add the following dependency:

```sbt
"com.softwaremill.sttp.openai" %% "core" % "0.3.3"
```

sttp-openai is available for Scala 2.13 and Scala 3

## Project content

OpenAI API Official Documentation https://platform.openai.com/docs/api-reference/completions

## Example

Examples are runnable using [scala-cli](https://scala-cli.virtuslab.org).

### To use ChatGPT

```scala mdoc:compile-only 
//> using dep com.softwaremill.sttp.openai::core:0.3.3

import sttp.openai.OpenAISyncClient
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message._

object Main extends App {
  val apiKey = System.getenv("OPENAI_KEY")
  val openAI = OpenAISyncClient(apiKey)

  // Create body of Chat Completions Request
  val bodyMessages: Seq[Message] = Seq(
    Message.UserMessage(
      content = Content.TextContent("Hello!"),
    )
  )

  // use ChatCompletionModel.CustomChatCompletionModel("gpt-some-future-version") 
  // for models not yet supported here
  val chatRequestBody: ChatBody = ChatBody(
    model = ChatCompletionModel.GPT4oMini,
    messages = bodyMessages
  )

  // be aware that calling `createChatCompletion` may throw an OpenAIException
  // e.g. AuthenticationException, RateLimitException and many more
  val chatResponse: ChatResponse = openAI.createChatCompletion(chatRequestBody)

  println(chatResponse)
  /*
      ChatResponse(
       chatcmpl-79shQITCiqTHFlI9tgElqcbMTJCLZ,chat.completion,
       1682589572,
       gpt-4o-mini,
       Usage(10,10,20),
       List(
         Choices(
           Message(assistant, Hello there! How can I assist you today?), stop, 0)
         )
       )
  */
}
```

### To use Ollama or Grok (OpenAI-compatible APIs)

Ollama with sync backend:

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::core:0.3.3

import sttp.model.Uri._
import sttp.openai.OpenAISyncClient
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message._

object Main extends App {
  // Create an instance of OpenAISyncClient providing any api key 
  // and a base url of locally running instance of ollama
  val openAI: OpenAISyncClient = OpenAISyncClient("ollama", uri"http://localhost:11434/v1")

  // Create body of Chat Completions Request
  val bodyMessages: Seq[Message] = Seq(
    Message.UserMessage(
      content = Content.TextContent("Hello!"),
    )
  )
  
  val chatRequestBody: ChatBody = ChatBody(
    // assuming one has already executed `ollama pull mistral` in console
    model = ChatCompletionModel.CustomChatCompletionModel("mistral"),
    messages = bodyMessages
  )

  // be aware that calling `createChatCompletion` may throw an OpenAIException
  // e.g. AuthenticationException, RateLimitException and many more
  val chatResponse: ChatResponse = openAI.createChatCompletion(chatRequestBody)

  println(chatResponse)
  /*
    ChatResponse(
      chatcmpl-650,
      List(
        Choices(
          Message(Assistant, """Hello there! How can I help you today?""", List(), None),
          "stop",
          0
        )
      ),
      1714663831,
      "mistral",
      "chat.completion",
      Usage(0, 187, 187),
      Some("fp_ollama")
    )
  */
}
```

Grok with cats-effect based backend:

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::core:0.3.3
//> using dep com.softwaremill.sttp.client4::cats:4.0.0-M17

import cats.effect.{ExitCode, IO, IOApp}
import sttp.client4.httpclient.cats.HttpClientCatsBackend

import sttp.model.Uri._
import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val apiKey = System.getenv("OPENAI_KEY")
    val openAI = new OpenAI(apiKey, uri"https://api.groq.com/openai/v1")

    val bodyMessages: Seq[Message] = Seq(
      Message.UserMessage(
        content = Content.TextContent("Hello!"),
      )
    )

    val chatRequestBody: ChatBody = ChatBody(
      model = ChatCompletionModel.CustomChatCompletionModel("gemma-7b-it"),
      messages = bodyMessages
    )
    
    HttpClientCatsBackend.resource[IO]().use { backend =>
      val response: IO[Either[OpenAIException, ChatResponse]] =
        openAI
          .createChatCompletion(chatRequestBody)
          .send(backend)
          .map(_.body)
      val rethrownResponse: IO[ChatResponse] = response.rethrow
      val redeemedResponse: IO[String] = rethrownResponse.redeem(
        error => error.getMessage,
        chatResponse => chatResponse.toString
      )
      redeemedResponse.flatMap(IO.println)
        .as(ExitCode.Success)
    }
  } 
  /*
    ChatResponse(
      "chatcmpl-e0f9f78c-5e74-494c-9599-da02fa495ff8",
      List(
        Choices(
          Message(Assistant, "Hello! 👋 It's great to hear from you. What can I do for you today? 😊", List(), None),
          "stop",
          0
        )
      ),
      1714667435,
      "gemma-7b-it",
      "chat.completion",
      Usage(16, 21, 37),
      Some("fp_f0c35fc854")
    )
  */
}
```

#### Available client implementations:

* `OpenAISyncClient` which provides high-level methods to interact with OpenAI. All the methods send requests synchronously and are blocking, might throw `OpenAIException`
* `OpenAI` which provides raw sttp-client4 `Request`s and parses `Response`s as `Either[OpenAIException, A]`

If you want to make use of other effects, you have to use `OpenAI` and pass the chosen backend directly to `request.send(backend)` function.

To customize a request when using the `OpenAISyncClient`, e.g. by adding a header, or changing the timeout (via request options), you can use the `.customizeRequest` method on the client.

Example below uses `HttpClientCatsBackend` as a backend, make sure to [add it to the dependencies](https://sttp.softwaremill.com/en/latest/backends/catseffect.html)
or use backend of your choice.

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::core:0.3.3
//> using dep com.softwaremill.sttp.client4::cats:4.0.0-M17

import cats.effect.{ExitCode, IO, IOApp}
import sttp.client4.httpclient.cats.HttpClientCatsBackend

import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val apiKey = System.getenv("OPENAI_KEY")
    val openAI = new OpenAI(apiKey)

    val bodyMessages: Seq[Message] = Seq(
      Message.UserMessage(
        content = Content.TextContent("Hello!"),
      )
    )

    val chatRequestBody: ChatBody = ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = bodyMessages
    )
    
    HttpClientCatsBackend.resource[IO]().use { backend =>
      val response: IO[Either[OpenAIException, ChatResponse]] =
        openAI
          .createChatCompletion(chatRequestBody)
          .send(backend)
          .map(_.body)
      val rethrownResponse: IO[ChatResponse] = response.rethrow
      val redeemedResponse: IO[String] = rethrownResponse.redeem(
        error => error.getMessage,
        chatResponse => chatResponse.toString
      )
      redeemedResponse.flatMap(IO.println)
        .as(ExitCode.Success)
    }
  } 
  /*
    ChatResponse(
      chatcmpl-79shQITCiqTHFlI9tgElqcbMTJCLZ,chat.completion,
      1682589572,
      gpt-3.5-turbo-0301,
      Usage(10,10,20),
      List(
        Choices(
          Message(assistant, Hello there! How can I assist you today?), stop, 0)
        )
      )
    )
  */
}
```

#### Create completion with streaming:

To enable streaming support for the Chat Completion API using server-sent events, you must include the appropriate
dependency for your chosen streaming library. We provide support for the following libraries: _fs2_, _ZIO_, _Akka / Pekko Streams_ and _Ox_.

For example, to use `fs2` add the following dependency & import:

```scala
// sbt dependency
"com.softwaremill.sttp.openai" %% "fs2" % "0.3.3"

// import 
import sttp.openai.streaming.fs2._
```

Example below uses `HttpClientFs2Backend` as a backend:

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::fs2:0.3.3

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import sttp.client4.httpclient.fs2.HttpClientFs2Backend

import sttp.openai.OpenAI
import sttp.openai.streaming.fs2._
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.requests.completions.chat.ChatChunkRequestResponseData.ChatChunkResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val apiKey = System.getenv("OPENAI_KEY")
    val openAI = new OpenAI(apiKey)

    val bodyMessages: Seq[Message] = Seq(
      Message.UserMessage(
        content = Content.TextContent("Hello!"),
      )
    )

    val chatRequestBody: ChatBody = ChatBody(
      model = ChatCompletionModel.GPT35Turbo,
      messages = bodyMessages
    )

    HttpClientFs2Backend.resource[IO]().use { backend =>
      val response: IO[Either[OpenAIException, Stream[IO, ChatChunkResponse]]] =
        openAI
          .createStreamedChatCompletion[IO](chatRequestBody)
          .send(backend)
          .map(_.body)

      response
        .flatMap {
          case Left(exception) => IO.println(exception.getMessage)
          case Right(stream)   => stream.evalTap(IO.println).compile.drain
        }
        .as(ExitCode.Success)
    }
  }
  /*
    ...
    ChatChunkResponse(
      "chatcmpl-8HEZFNDmu2AYW8jVvNKyRO4W4KcO8",
      "chat.completion.chunk",
      1699118265,
      "gpt-3.5-turbo-0613",
      List(
        Choices(
          Delta(None, Some("Hi"), None),
          null,
          0
        )
      )
    )
    ...
    ChatChunkResponse(
      "chatcmpl-8HEZFNDmu2AYW8jVvNKyRO4W4KcO8",
      "chat.completion.chunk",
      1699118265,
      "gpt-3.5-turbo-0613",
      List(
        Choices(
          Delta(None, Some(" there"), None),
          null,
          0
        )
      )
    )
    ...
   */
}
```

To use direct-style streaming (requires Scala 3) add the following dependency & import:

```scala
// sbt dependency
"com.softwaremill.sttp.openai" %% "ox" % "0.3.3"

// import 
import sttp.openai.streaming.ox.*
```

Example code:

```scala
//> using dep com.softwaremill.sttp.openai::ox:0.3.3

import ox.*
import ox.either.orThrow
import sttp.client4.DefaultSyncBackend
import sttp.openai.OpenAI
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message.*
import sttp.openai.streaming.ox.*

object Main extends OxApp:
  override def run(args: Vector[String])(using Ox): ExitCode =
    val apiKey = System.getenv("OPENAI_KEY")
    val openAI = new OpenAI(apiKey)
    
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
    openAI
      .createStreamedChatCompletion(chatRequestBody)
      .send(backend)
      .body // this gives us an Either[OpenAIException, Flow[ChatChunkResponse]]
      .orThrow // we choose to throw any exceptions and fail the whole app
      .runForeach(el => println(el.orThrow))
    
    ExitCode.Success
```

See also the [ChatProxy](https://github.com/softwaremill/sttp-openai/blob/master/examples/src/main/scala/examples/ChatProxy.scala) example application.

#### Structured Outputs/JSON Schema support

To take advantage of [OpenAI's Structured Outputs](https://platform.openai.com/docs/guides/structured-outputs/introduction)
and support for JSON Schema, you can use `ResponseFormat.JsonSchema` when creating a completion.

The example below produces a JSON object:

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::core:0.3.3

import scala.collection.immutable.ListMap
import sttp.apispec.{Schema, SchemaType}
import sttp.openai.OpenAISyncClient
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel, ResponseFormat}
import sttp.openai.requests.completions.chat.message._

object Main extends App {
  val apiKey = System.getenv("OPENAI_KEY")
  val openAI = OpenAISyncClient(apiKey)

  val jsonSchema: Schema =
    Schema(SchemaType.Object).copy(properties =
      ListMap(
        "steps" -> Schema(SchemaType.Array).copy(items =
          Some(Schema(SchemaType.Object).copy(properties =
            ListMap(
              "explanation" -> Schema(SchemaType.String),
              "output" -> Schema(SchemaType.String)
            )
          ))
        ),
        "finalAnswer" -> Schema(SchemaType.String)
      ),
    )

  val responseFormat: ResponseFormat.JsonSchema =
    ResponseFormat.JsonSchema(
      name = "mathReasoning",
      strict = true,
      schema = jsonSchema
    )

  val bodyMessages: Seq[Message] = Seq(
    Message.SystemMessage(content = "You are a helpful math tutor. Guide the user through the solution step by step."),
    Message.UserMessage(content = Content.TextContent("How can I solve 8x + 7 = -23"))
  )

  // Create body of Chat Completions Request, using our JSON Schema as the `responseFormat`
  val chatRequestBody: ChatBody = ChatBody(
    model = ChatCompletionModel.GPT4oMini,
    messages = bodyMessages,
    responseFormat = Some(responseFormat)
  )

  val chatResponse: ChatResponse = openAI.createChatCompletion(chatRequestBody)

  println(chatResponse.choices)
  /*
    List(
      Choices(
        Message(
          Assistant,
          {
            "steps": [
              {"explanation": "Start with the original equation: 8x + 7 = -23", "output": "8x + 7 = -23"},
              {"explanation": "Subtract 7 from both sides to isolate the term with x.", "output": "8x + 7 - 7 = -23 - 7"},
              {"explanation": "This simplifies to: 8x = -30", "output": "8x = -30"},
              {"explanation": "Now, divide both sides by 8 to solve for x.", "output": "x = -30 / 8"},
              {"explanation": "Simplify -30 / 8 to its simplest form. Both the numerator and denominator can be divided by 2.", "output": "x = -15 / 4"}
            ],
            "finalAnswer": "x = -15/4"
          },
          List(),
          None
        ),
        stop,
        0
      )
    )
  */
}
```

##### Deriving a JSON Schema with tapir

To derive the same math reasoning schema used above, you can use
[Tapir's support for generating a JSON schema from a Tapir schema](https://tapir.softwaremill.com/en/latest/docs/json-schema.html):

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.tapir::tapir-apispec-docs:1.11.7

import sttp.apispec.{Schema => ASchema}
import sttp.tapir.Schema
import sttp.tapir.docs.apispec.schema.TapirSchemaToJsonSchema
import sttp.tapir.generic.auto._

case class Step(
  explanation: String,
  output: String
)

case class MathReasoning(
  steps: List[Step],
  finalAnswer: String
)

val tSchema = implicitly[Schema[MathReasoning]]

val jsonSchema: ASchema = TapirSchemaToJsonSchema(
  tSchema,
  markOptionsAsNullable = true
)
```

#### Generating JSON Schema from case class

We can also generate JSON Schema directly from case class, without defining the schema manually.

In the example below I define such use case. User tries to book a flight, using function tool. The flow looks as follows:
- User sends a message with the request to book a flight and provides function tool, which means that there is a function on a client side which 'knows' how to book a flight. Within this call it is necessary to provide Json Schema to define function arguments.
- Assistant sends a message with arguments created based on Json Schema provided in the first step.
- User calls custom function with arguments sent by Assistant before.
- User sends result from the function call to Assistant.
- Assistant sends a final result to User.

The key point here is introducing SchematizedFunctionTool class. With this class in place Json Schema can be automatically generated using TapirSchemaToJsonSchema functionality. All we need to do is to define case class with [Tapir Schema](https://tapir.softwaremill.com/en/latest/endpoint/schemas.html) defined for it.

Another helpful feature is adding possibility to create ToolMessage object passing object instead of String, which will be automatically serialized to Json. All you have to do is just define SnakePickle.Writer for specific class.

With all this in mind please remember that it is still required to deserialized arguments, which are sent back by Assistant to call our function.

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::core:0.3.3

import sttp.openai.OpenAISyncClient
import sttp.openai.json.SnakePickle
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatCompletionModel.GPT4oMini
import sttp.openai.requests.completions.chat.ToolCall.FunctionToolCall
import sttp.openai.requests.completions.chat.message.Content.TextContent
import sttp.openai.requests.completions.chat.message.Message.{AssistantMessage, ToolMessage, UserMessage}
import sttp.openai.requests.completions.chat.message.Tool.SchematizedFunctionTool
import sttp.tapir.generic.auto._

case class Passenger(name: String, age: Int)

object Passenger {
  implicit val passengerR: SnakePickle.Reader[Passenger] = SnakePickle.macroR[Passenger]
}

case class FlightDetails(passenger: Passenger, departureCity: String, destinationCity: String)

object FlightDetails {
  implicit val flightDetailsR: SnakePickle.Reader[FlightDetails] = SnakePickle.macroR[FlightDetails]
}

case class BookedFlight(confirmationNumber: String, status: String)

object BookedFlight {
  implicit val bookedFlightW: SnakePickle.Writer[BookedFlight] = SnakePickle.macroW[BookedFlight]
}

object Main extends App {
  val apiKey = System.getenv("OPENAI_KEY")
  val openAI = OpenAISyncClient(apiKey)

  val initialRequestMessage = Seq(UserMessage(content = TextContent("I want to book a flight from London to Tokyo for Jane Doe, age 34")))

  // Request created using SchematizedFunctionTool, all we need to do here is just define the type. The schema is automatically generated using a macro, available via the `sttp.tapir.generic.auto._` import.
  val givenRequest = ChatBody(
    model = GPT4oMini,
    messages = initialRequestMessage,
    tools = Some(Seq(
      SchematizedFunctionTool[FlightDetails](
        description = "Books a flight for a passenger with full details",
        name = "book_flight"))
    )
  )

  val initialRequestResult = openAI.createChatCompletion(givenRequest)

  println(initialRequestResult.choices)
  /*
    List(
      Choices(
        Message(
          null,
          None,
          List(
            FunctionToolCall(
              Some(call_XZNvfldLQTa1f7aMInswpTMS),
              FunctionCall(
                {
                  "passenger": {
                    "name": "Jane Doe",
                    "age": 34
                  },
                  "departureCity": "London",
                  "destinationCity": "Tokyo"
                },
                Some(book_flight)
              )
            )
          ),
          Assistant,
          None,
          None
        ),
        tool_calls,
        0,
        None
      )
    )
    */

  // Tool calls list (in this example it is just single tool call, but there may be multiple), which is necessary to build message list for second request.
  val toolCalls = initialRequestResult.choices.head.message.toolCalls

  val functionToolCall = toolCalls.head match {
    case functionToolCall: FunctionToolCall => functionToolCall
  }

  // Function arguments are manually deserialized, 'bookFlight' function mimic external function definition.
  val bookedFlight = bookFlight(SnakePickle.read[FlightDetails](functionToolCall.function.arguments))

  val secondRequest = givenRequest.copy(
    messages = initialRequestMessage
      :+ AssistantMessage(content = "", toolCalls = toolCalls)
      // ToolMessage created using object instead of String with Json representation of object.
      :+ ToolMessage(toolCallId = functionToolCall.id.get, content = bookedFlight)
  )

  val finalResult = openAI.createChatCompletion(secondRequest)

  println(finalResult.choices)
  /*
    List(
      Choices(
        Message(
          "The flight from London to Tokyo for Jane Doe, age 34, has been successfully booked. The confirmation number is **123456** and the status is **confirmed**.",
          None,
          List(),
          Assistant,
          None,
          None
        ),
        stop,
        0,
        None
      )
    )
    */

  def bookFlight(flightDetails: FlightDetails): BookedFlight = {
    println(flightDetails)
    BookedFlight(confirmationNumber = "123456", status = "confirmed")
  }

}
```

## Contributing

If you have a question, or hit a problem, feel free to post on our community https://softwaremill.community/c/open-source/

Or, if you encounter a bug, something is unclear in the code or documentation, don’t hesitate and open an issue on GitHub.

## Commercial Support

We offer commercial support for sttp and related technologies, as well as development services. [Contact us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2023-2024 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
