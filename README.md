![sttp-model](https://github.com/softwaremill/sttp-openai/raw/master/banner.png)


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

[//]: # (```scala)
[//]: # ("com.softwaremill.sttp.openai" %% "core" % "?.?.?")
[//]: # (```)

sttp openai is available for Scala 2.13 and Scala 3

## Project content

OpenAI API Offical Documentation https://platform.openai.com/docs/api-reference/completions

### Not yet implemented:
  * Create chat completions SSE
  * Create completions SSE
  * List fine-tune events SSE

## Example

### To use ChatGPT

```scala mdoc:compile-only 
import sttp.client4._
import sttp.openai.requests.completions.chat.ChatRequestResponseData.ChatResponse
import sttp.openai.requests.completions.chat.ChatRequestBody.ChatBody
import sttp.openai.requests.completions.chat.Message

// Create an instance of OpenAi providing your API secret-key

val openAi: OpenAi = new OpenAi("your-secret-key")

// Create body of Chat Completions Request

val bodyMessages: Seq[Message] = Seq(
  Message(
    role = "user",
    content = "Hello!"
  )
)

val chatRequestBody: ChatBody = ChatBody(
  model = "gpt-3.5-turbo",
  messages = bodyMessages
)

// Use createChatCompletion and pass created request body to create sttp request

val request = openAi.createChatCompletion(chatRequestBody)

// To invoke request and get a response provide your wished backend and send created request

val backend: SyncBackend = DefaultSyncBackend()

val response = request.send(backend)

println(response)
/*
 Right(
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
```

## Contributing

If you have a question, or hit a problem, feel free to post on our community https://softwaremill.community/c/open-source/

Or, if you encounter a bug, something is unclear in the code or documentation, don’t hesitate and open an issue on GitHub.

## Commercial Support

We offer commercial support for sttp and related technologies, as well as development services. [Contact us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2023 SoftwareMill [https://softwaremill.com](https://softwaremill.com).