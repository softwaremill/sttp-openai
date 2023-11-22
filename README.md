![sttp-model](https://github.com/softwaremill/sttp-openai/raw/master/banner.jpg)


[![Ideas, suggestions, problems, questions](https://img.shields.io/badge/Discourse-ask%20question-blue)](https://softwaremill.community/c/tapir)
[![CI](https://github.com/softwaremill/sttp-openai/workflows/CI/badge.svg)](https://github.com/softwaremill/sttp-openai/actions?query=workflow%3ACI+branch%3Amaster)

[//]: # ([![Maven Central]&#40;https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp.openai.svg&#41;&#40;https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp.openai&#41;)
sttp is a family of Scala HTTP-related projects, and currently includes:

* [sttp client](https://github.com/softwaremill/sttp): The Scala HTTP client you always wanted!
* [sttp tapir](https://github.com/softwaremill/tapir): Typed API descRiptions
* sttp openai: this project. Scala client wrapper for OpenAI API. Use the power of ChatGPT inside your code!

## Intro
Sttp-openai uses sttp client to describe requests and responses used in OpenAI endpoints.

## Quickstart with sbt

Add the following dependency:

```sbt
"com.softwaremill.sttp.openai" %% "core" % "0.0.8"
```

sttp openai is available for Scala 2.13 and Scala 3

## Project content

OpenAI API Official Documentation https://platform.openai.com/docs/api-reference/completions

## Example

### To use ChatGPT

```scala mdoc:compile-only 
import sttp.openai.OpenAISyncClient
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message._

object Main extends App {
  // Create an instance of OpenAISyncClient providing your API secret-key
  val openAI: OpenAISyncClient = OpenAISyncClient("your-secret-key")

  // Create body of Chat Completions Request
  val bodyMessages: Seq[Message] = Seq(
    Message.UserMessage(
      content = Content.TextContent("Hello!"),
    )
  )

  val chatRequestBody: ChatBody = ChatBody(
    model = ChatCompletionModel.GPT35Turbo,
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
       gpt-3.5-turbo-0301,
       Usage(10,10,20),
       List(
         Choices(
           Message(assistant, Hello there! How can I assist you today?), stop, 0)
         )
       )
  */
}
```
#### Available backend implementations:
* `OpenAISyncBackend` which uses identity monad `Id[A]` as an effect `F[A]` and throws `OpenAIException`
* `OpenAI` which provides raw sttp `Request`s and wraps `Response`s into `Either[OpenAIException, A]`

If you want to make use of other effects, you have to use `OpenAI` and pass the chosen backend directly to `request.send(backend)` function.

Example below uses `HttpClientCatsBackend` as a backend, make sure to [add it to the dependencies](https://sttp.softwaremill.com/en/latest/backends/catseffect.html)
or use backend of your choice.

```scala mdoc:compile-only 
import cats.effect.{ExitCode, IO, IOApp}
import sttp.client4.httpclient.cats.HttpClientCatsBackend

import sttp.openai.OpenAI
import sttp.openai.OpenAIExceptions.OpenAIException
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.{ChatBody, ChatCompletionModel}
import sttp.openai.requests.completions.chat.message._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val openAI: OpenAI = new OpenAI("your-secret-key")

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
dependency for your chosen streaming library. We provide support for the following libraries: _Fs2_, _ZIO_, _Akka / Pekko Streams_

For example, to use `Fs2` add the following import:

```scala
import sttp.openai.streaming.fs2._
```

Example below uses `HttpClientFs2Backend` as a backend.

```scala mdoc:compile-only
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
    val openAI: OpenAI = new OpenAI("your-secret-key")

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

## Contributing

If you have a question, or hit a problem, feel free to post on our community https://softwaremill.community/c/open-source/

Or, if you encounter a bug, something is unclear in the code or documentation, don’t hesitate and open an issue on GitHub.

## Commercial Support

We offer commercial support for sttp and related technologies, as well as development services. [Contact us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2023 SoftwareMill [https://softwaremill.com](https://softwaremill.com).