![sttp-model](https://github.com/softwaremill/sttp-model/raw/master/banner.png) Banner should be changed to sttp openAi

[![Join the chat at https://gitter.im/softwaremill/sttp](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/softwaremill/sttp)
[![CI](https://github.com/softwaremill/sttp-openai/workflows/CI/badge.svg)](https://github.com/softwaremill/sttp-openai/actions?query=workflow%3ACI+branch%3Amaster)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp)(https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp)

sttp is a family of Scala HTTP-related projects, and currently includes:

* [sttp client](https://github.com/softwaremill/sttp): The Scala HTTP client you always wanted!
* [sttp tapir](https://github.com/softwaremill/tapir): Typed API descRiptions
* sttp openai: this project. Scala client wrapper for OpenAi API. Use the power of ChatGPT inside your code!

## Quickstart with sbt

Add the following dependency:

```scala
"com.softwaremill.sttp.openai" %% "core" % "?.?.?"
```

sttp openai is available for Scala 2.12 and Scala 3.0.0

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
