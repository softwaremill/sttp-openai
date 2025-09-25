# Claude API Module for sttp-openai

This module provides **native support for Anthropic's Claude API** within the sttp-openai library. Unlike OpenAI compatibility layers, this provides direct access to Claude's unique features and API structure.

## Features

- ✅ **Native Claude API support** - Direct Claude API integration, not compatibility layer
- ✅ **ContentBlock structure** - Support for Claude's rich message content blocks (text, images)
- ✅ **Proper Authentication** - Uses `x-api-key` and `anthropic-version` headers
- ✅ **Messages API** - Complete `/v1/messages` endpoint implementation
- ✅ **Models API** - List available Claude models via `/v1/models`
- ✅ **Streaming Support** - Server-Sent Events streaming for all effect systems (fs2, ZIO, Akka, Pekko, Ox)
- ✅ **Tool Calling** - Native Claude tool calling support
- ✅ **Image Support** - Multi-modal inputs via ContentBlock with base64 encoding
- ✅ **Comprehensive Error Handling** - Claude-specific exception hierarchy
- ✅ **System Messages** - Proper system message handling via `system` parameter
- ✅ **Cross-platform** - Support for Scala 2.13 and Scala 3

## Quick Start

### Installation

Add to your `build.sbt`:

```scala
libraryDependencies += "com.softwaremill.sttp.openai" %% "claude" % "0.3.7"

// For streaming support, add one or more:
libraryDependencies += "com.softwaremill.sttp.openai" %% "claude-streaming-fs2" % "0.3.7"
libraryDependencies += "com.softwaremill.sttp.openai" %% "claude-streaming-zio" % "0.3.7"
libraryDependencies += "com.softwaremill.sttp.openai" %% "claude-streaming-akka" % "0.3.7"
libraryDependencies += "com.softwaremill.sttp.openai" %% "claude-streaming-pekko" % "0.3.7"
libraryDependencies += "com.softwaremill.sttp.openai" %% "claude-streaming-ox" % "0.3.7" // Scala 3 only
```

### Basic Usage

```scala
import sttp.ai.claude._
import sttp.ai.claude.config.ClaudeConfig
import sttp.ai.claude.models.{ContentBlock, Message}
import sttp.ai.claude.requests.MessageRequest
import sttp.client4._

// Configuration - from environment or explicit
val config = ClaudeConfig.fromEnv // uses ANTHROPIC_API_KEY
// Or explicit:
// val config = ClaudeConfig(apiKey = "your-api-key")

val backend = DefaultSyncBackend()
val client = ClaudeClient(config)

// Simple text message
val messages = List(
  Message.user(List(ContentBlock.text("Hello Claude!")))
)

val request = MessageRequest.simple(
  model = "claude-3-sonnet-20240229",
  messages = messages,
  maxTokens = 1000
)

// Synchronous request
val response = client.createMessage(request).send(backend)
response match {
  case Right(messageResponse) =>
    println(messageResponse.content.head.text.getOrElse(""))
  case Left(error) =>
    println(s"Error: ${error.getMessage}")
}
```

## API Documentation

### Configuration

```scala
case class ClaudeConfig(
  apiKey: String,                                    // Your Anthropic API key
  anthropicVersion: String = "2023-06-01",          // API version header
  baseUrl: Uri = "https://api.anthropic.com",       // API base URL
  timeout: Duration = 60.seconds,                   // Request timeout
  maxRetries: Int = 3,                             // Max retry attempts
  organization: Option[String] = None               // Optional organization ID
)
```

**Environment Variables:**
- `ANTHROPIC_API_KEY` - Your API key (required)
- `ANTHROPIC_VERSION` - API version (optional, defaults to "2023-06-01")
- `ANTHROPIC_BASE_URL` - Custom base URL (optional)

### Messages API

#### Basic Text Conversation

```scala
val messages = List(
  Message.user(List(ContentBlock.text("What is the capital of France?"))),
  Message.assistant(List(ContentBlock.text("The capital of France is Paris."))),
  Message.user(List(ContentBlock.text("What about Italy?")))
)

val request = MessageRequest.simple(
  model = "claude-3-sonnet-20240229",
  messages = messages,
  maxTokens = 1000
)
```

#### System Messages

Unlike OpenAI, Claude uses a separate `system` parameter instead of system role messages:

```scala
val request = MessageRequest.withSystem(
  model = "claude-3-sonnet-20240229",
  system = "You are a helpful assistant that always responds in French.",
  messages = List(Message.user(List(ContentBlock.text("Hello!")))),
  maxTokens = 1000
)
```

#### Image Support

```scala
import java.util.Base64
import java.nio.file.{Files, Paths}

// Read and encode image
val imageBytes = Files.readAllBytes(Paths.get("image.jpg"))
val base64Image = Base64.getEncoder.encodeToString(imageBytes)

val messages = List(
  Message.user(List(
    ContentBlock.text("What do you see in this image?"),
    ContentBlock.image(
      mediaType = "image/jpeg",
      data = base64Image
    )
  ))
)

val request = MessageRequest.simple(
  model = "claude-3-sonnet-20240229",
  messages = messages,
  maxTokens = 1000
)
```

#### Advanced Parameters

```scala
val request = MessageRequest(
  model = "claude-3-sonnet-20240229",
  messages = messages,
  maxTokens = 4000,
  temperature = Some(0.7),           // Creativity (0.0 - 1.0)
  topP = Some(0.9),                  // Nucleus sampling
  topK = Some(40),                   // Top-k sampling
  stopSequences = Some(List("\n\n")), // Stop generation at sequences
  system = Some("Be concise and helpful."),
  tools = Some(tools)                // Tool calling support
)
```

### Tool Calling

```scala
import sttp.ai.claude.models.{Tool, ToolInputSchema, PropertySchema}

val weatherTool = Tool(
  name = "get_weather",
  description = "Get current weather for a location",
  inputSchema = ToolInputSchema(
    `type` = "object",
    properties = Map(
      "location" -> PropertySchema(`type` = "string", description = Some("City name")),
      "unit" -> PropertySchema(`type` = "string", enum = Some(List("celsius", "fahrenheit")))
    ),
    required = Some(List("location"))
  )
)

val request = MessageRequest.withTools(
  model = "claude-3-sonnet-20240229",
  messages = List(Message.user(List(ContentBlock.text("What's the weather in Paris?")))),
  maxTokens = 1000,
  tools = List(weatherTool)
)
```

### Streaming

#### Using fs2 (cats-effect)

```scala
import sttp.ai.claude.streaming.fs2._
import sttp.client4.httpclient.fs2.HttpClientFs2Backend
import cats.effect.IO

val backend = HttpClientFs2Backend[IO]()

// Extension method for streaming
val streamRequest = client.createMessageAsBinaryStream(backend.capabilities.streams, request)

streamRequest
  .send(backend)
  .map(_.map(_.parseSSE.parseClaudeStreamResponse))
  .flatMap {
    case Right(stream) =>
      stream
        .evalTap(response => IO.println(response.delta.text.getOrElse("")))
        .compile
        .drain
    case Left(error) =>
      IO.println(s"Error: $error")
  }
```

#### Using ZIO

```scala
import sttp.ai.claude.streaming.zio._
import sttp.client4.httpclient.zio.HttpClientZioBackend
import zio._

val backend = HttpClientZioBackend()

val program = for {
  streamRequest <- ZIO.succeed(client.createMessageAsBinaryStream(backend.capabilities.streams, request))
  result <- streamRequest.send(backend)
  _ <- result match {
    case Right(stream) =>
      stream
        .parseSSE
        .parseClaudeStreamResponse
        .tap(response => Console.printLine(response.delta.text.getOrElse("")))
        .runDrain
    case Left(error) =>
      Console.printLine(s"Error: $error")
  }
} yield ()
```

#### Using Ox (Scala 3)

```scala
import sttp.ai.claude.streaming.ox._
import sttp.client4.ox.OxHttpClientBackend
import ox._

val backend = OxHttpClientBackend()

val streamRequest = client.createMessageAsBinaryStream(backend.capabilities.streams, request)

val result = streamRequest.send(backend)
result match {
  case Right(stream) =>
    stream
      .parseSSE
      .parseClaudeStreamResponse
      .tap(response => println(response.delta.text.getOrElse("")))
      .runDrain()
  case Left(error) =>
    println(s"Error: $error")
}
```

### Models API

```scala
val modelsRequest = client.listModels()
val models = modelsRequest.send(backend)

models match {
  case Right(response) =>
    response.data.foreach(model => println(s"${model.id} - ${model.displayName.getOrElse("N/A")}"))
  case Left(error) =>
    println(s"Error: $error")
}
```

## Error Handling

Claude-specific exception hierarchy:

```scala
import sttp.ai.claude.ClaudeExceptions._

client.createMessage(request).send(backend) match {
  case Right(response) => // Success
    handleResponse(response)
  case Left(error) => error match {
    case _: AuthenticationException => // Invalid API key
      println("Authentication failed - check your API key")
    case _: RateLimitException => // Rate limited
      println("Rate limited - please wait before retrying")
    case _: InvalidRequestException => // Malformed request
      println("Invalid request - check your parameters")
    case _: PermissionException => // Access denied
      println("Permission denied for this resource")
    case _: APIException => // Other API error
      println(s"API error: ${error.getMessage}")
    case _: DeserializationClaudeException => // JSON parsing error
      println("Failed to parse response")
  }
}
```

## Key Differences from OpenAI API

| Feature | Claude API | OpenAI API |
|---------|------------|------------|
| **Message Content** | `ContentBlock` arrays | Simple strings |
| **System Messages** | `system` parameter | Role-based message |
| **Authentication** | `x-api-key` + `anthropic-version` headers | `Authorization` header |
| **Image Input** | ContentBlock with base64 | URL or base64 in content |
| **Tool Calling** | Native tool structure | Function calling |
| **Streaming** | Server-Sent Events | Server-Sent Events |
| **Model Names** | `claude-3-sonnet-20240229` | `gpt-4` |

## Available Models

Common Claude models (use `listModels()` for current list):

- `claude-3-sonnet-20240229` - Balanced performance and speed
- `claude-3-opus-20240229` - Highest capability model
- `claude-3-haiku-20240307` - Fastest model
- `claude-instant-1.2` - Legacy fast model

## Rate Limiting and Best Practices

1. **Handle Rate Limits**: Implement exponential backoff for 429 responses
2. **Batch Requests**: Combine multiple requests when possible
3. **Monitor Usage**: Track token usage via `Usage` in responses
4. **Optimize Context**: Keep context windows reasonable for better performance
5. **Use Appropriate Models**: Choose the right model for your use case

## Examples

See the `examples/` directory for complete runnable examples including:
- Basic chat completion
- Streaming with different effect systems
- Image analysis
- Tool calling
- Error handling patterns

## Synchronous Client

For blocking operations, use `ClaudeSyncClient`:

```scala
import sttp.ai.claude.ClaudeSyncClient

val syncClient = new ClaudeSyncClient(config)

// Throws ClaudeException on error
try {
  val response = syncClient.createMessage(request)
  println(response.content.head.text.getOrElse(""))
} catch {
  case e: ClaudeException => println(s"Error: ${e.getMessage}")
}
```

## Contributing

This module follows the same patterns as the main sttp-openai library. When contributing:

1. Run `sbt scalafmtAll` after changes
2. Add tests for new functionality
3. Update documentation
4. Follow existing code patterns

## Support

- [Claude API Documentation](https://docs.anthropic.com/claude/reference/)
- [sttp-openai Issues](https://github.com/softwaremill/sttp-openai/issues)
- [sttp Documentation](https://sttp.softwaremill.com/en/latest/)