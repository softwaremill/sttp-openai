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
"com.softwaremill.sttp.openai" %% "core" % "0.2.2"
```

sttp-openai is available for Scala 2.13 and Scala 3

## Project content

OpenAI API Official Documentation https://platform.openai.com/docs/api-reference/completions

## Example

Examples are runnable using [scala-cli](https://scala-cli.virtuslab.org).

### To use ChatGPT

```scala mdoc:compile-only 
//> using dep com.softwaremill.sttp.openai::core:0.2.2

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

  // use ChatCompletionModel.CustomChatCompletionModel("gpt-some-future-version") for models not yet supported here
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
//> using dep com.softwaremill.sttp.openai::core:0.2.2

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
//> using dep com.softwaremill.sttp.openai::core:0.2.2
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

Example below uses `HttpClientCatsBackend` as a backend, make sure to [add it to the dependencies](https://sttp.softwaremill.com/en/latest/backends/catseffect.html)
or use backend of your choice.

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::core:0.2.2
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
"com.softwaremill.sttp.openai" %% "fs2" % "0.2.2"

// import 
import sttp.openai.streaming.fs2._
```

Example below uses `HttpClientFs2Backend` as a backend:

```scala mdoc:compile-only
//> using dep com.softwaremill.sttp.openai::fs2:0.2.2

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
"com.softwaremill.sttp.openai" %% "ox" % "0.2.2"

// import 
import sttp.openai.streaming.ox.*
```

Example code:

```scala
//> using dep com.softwaremill.sttp.openai::ox:0.2.2

import ox.*
import ox.either.orThrow
import sttp.client4.DefaultSyncBackend
import sttp.openai.OpenAI
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message.*
import sttp.openai.streaming.ox.*

object Main extends OxApp:
  override def run(args: Vector[String])(using Ox, IO): ExitCode =
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
    supervised {
      val source = openAI
        .createStreamedChatCompletion(chatRequestBody)
        .send(backend)
        .body // this gives us an Either[OpenAIException, Source[ChatChunkResponse]]
        .orThrow // we choose to throw any exceptions and fail the whole                                                                      
    
      source.foreach(el => println(el.orThrow))
    }
    
    ExitCode.Success
```

## Contributing

If you have a question, or hit a problem, feel free to post on our community https://softwaremill.community/c/open-source/

Or, if you encounter a bug, something is unclear in the code or documentation, don’t hesitate and open an issue on GitHub.

## Commercial Support

We offer commercial support for sttp and related technologies, as well as development services. [Contact us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2023-2024 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
