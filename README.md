![sttp-model](https://github.com/softwaremill/sttp-openai/raw/master/banner.png)

[![Join the chat at https://gitter.im/softwaremill/sttp](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/softwaremill/sttp)
[![CI](https://github.com/softwaremill/sttp-openai/workflows/CI/badge.svg)](https://github.com/softwaremill/sttp-openai/actions?query=workflow%3ACI+branch%3Amaster)

[//]: # ([![Maven Central]&#40;https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp&#41;&#40;https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp&#41;)

sttp is a family of Scala HTTP-related projects, and currently includes:

* [sttp client](https://github.com/softwaremill/sttp): The Scala HTTP client you always wanted!
* [sttp tapir](https://github.com/softwaremill/tapir): Typed API descRiptions
* sttp openai: this project. Scala client wrapper for OpenAI API. Use the power of ChatGPT inside your code!

## Quickstart with sbt

Add the following dependency:

[//]: # (```scala)
[//]: # ("com.softwaremill.sttp.openai" %% "core" % "?.?.?")
[//]: # (```)

sttp openai is available for Scala 2.12 and Scala 3

## Project content

Available endpoints with modeled response classes include:
* Models:
  * Retrieves all the available models
  * Retrieves model for given ID
* Completions
  * Creates a completion for provided model and prompt
* Chat
  * Creates a completion for the provided prompt and parameters
* Edits
  * Creates a new edit for the provided input, instruction, and parameters
* Images
  * Creates an image given a prompt (Returns an url with an image)
  * Creates an edited or extended image given an original image and a prompt (Returns an url with an image)
  * Creates a variation of a given image (Returns an url with an image)
* Embeddings
  * Creates an embedding vector representing the input text
* Audio
  * Transcribes audio into the input language
  * Translates audio into English
* Files
  * Returns a list of files that belong to the user's organization
  * Upload a file that contains document(s) to be used across various endpoints/features
  * Delete a file for given ID
  * Returns information about a specific file providing file's ID
  * Returns the contents of the specified file providing file's ID
* Fine-Tunes
  * Creates a job that fine-tunes a specified model from a given dataset
  * Retrieves all your organization's fine-tuning jobs
  * Retrieves info about the fine-tune job providing fine-tune's ID
  * Immediately cancel a fine-tune job providing fine-tune's ID
  * Retrieves fine-grained status updates for a fine-tune job providing fine-tune's ID
  * Delete a fine-tuned model providing fine-tune's ID
* Moderation
  * Classifies if text violates OpenAI's Content Policy

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

val request: Request[Either[ResponseException[String, Exception], ChatResponse]] = openAi.createChatCompletion(chatRequestBody)

// To invoke request and get a response provide your wished backend and send created request

val backend: SyncBackend = DefaultSyncBackend()

val response: Response[Either[ResponseException[String, Exception], ChatResponse]] = request.send(backend)
```

## Contributing

If you have a question, or hit a problem, feel free to post on our community https://softwaremill.community/c/sttp-client/

Or, if you encounter a bug, something is unclear in the code or documentation, don’t hesitate and open an issue on GitHub.

## Commercial Support

We offer commercial support for sttp and related technologies, as well as development services. [Contact us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2023 SoftwareMill [https://softwaremill.com](https://softwaremill.com).