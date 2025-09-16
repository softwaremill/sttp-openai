# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

sttp-openai is a Scala library providing a non-official client wrapper for OpenAI (and OpenAI-compatible) APIs. It's built on top of the sttp HTTP client library and supports both sync and async operations with various effect systems (cats-effect, ZIO, Akka/Pekko Streams, Ox).

## Project Structure

### Root Directory Layout
```
sttp-openai/
├── core/                    # Main library code
├── streaming/               # Effect-system specific streaming implementations
│   ├── fs2/                # cats-effect/fs2 streaming support
│   ├── zio/                # ZIO streaming support
│   ├── akka/               # Akka Streams support (Scala 2.13 only)
│   ├── pekko/              # Pekko Streams support
│   └── ox/                 # Ox direct-style streaming (Scala 3 only)
├── examples/               # Runnable examples using scala-cli
├── model_update_scripts/   # Automation for updating OpenAI model definitions
├── project/                # sbt build configuration
├── generated-docs/         # Generated documentation (mdoc output)
└── .github/workflows/      # CI/CD GitHub Actions
```

### Core Module Structure (`core/src/main/scala/sttp/openai/`)
```
sttp.openai/
├── OpenAI.scala                 # Raw sttp requests returning Either[OpenAIException, A]
├── OpenAISyncClient.scala       # High-level sync client that may throw exceptions
├── OpenAIExceptions.scala       # Exception hierarchy for API errors
├── json/                        # JSON serialization utilities
│   ├── SnakePickle.scala       # Snake_case conversion for uPickle
│   └── SttpUpickleApiExtension.scala
└── requests/                    # API endpoint definitions (mirrors OpenAI API structure)
```

### Requests Package Organization (`requests/`)
The `requests/` package closely mirrors OpenAI's API structure. Each directory contains request/response models and configurations:

```
requests/
├── completions/             # Text completion APIs
│   ├── chat/               # Chat completions (most complex)
│   │   ├── message/        # Message types, content, tools
│   │   ├── ChatRequestBody.scala
│   │   ├── ChatRequestResponseData.scala
│   │   ├── ChatChunkRequestResponseData.scala  # Streaming responses
│   │   └── ToolCall.scala
│   ├── CompletionsRequestBody.scala
│   └── CompletionsResponseData.scala
├── audio/                   # Audio APIs
│   ├── speech/             # Text-to-speech
│   ├── transcriptions/     # Speech-to-text
│   └── translations/       # Audio translation
├── images/                  # Image generation APIs
│   ├── creation/           # Image generation
│   ├── edit/               # Image editing
│   └── variations/         # Image variations
├── embeddings/             # Text embeddings API
├── assistants/             # OpenAI Assistants API
├── threads/                # Conversation threads
│   ├── messages/          # Thread messages
│   └── runs/              # Thread runs and tool calls
├── vectorstore/            # Vector stores and file management
│   └── file/              # Vector store file operations
├── files/                  # File upload/management
├── models/                 # Model listing and management
├── moderations/            # Content moderation
├── finetuning/            # Model fine-tuning
├── batch/                 # Batch API requests
├── admin/                 # Admin/organization APIs
├── responses/             # Response generation API
└── upload/                # Multipart upload handling
```

### Streaming Modules Pattern
Each streaming module follows a consistent structure:
```
streaming/{effect-system}/src/main/scala/sttp/openai/streaming/{effect}/
└── package.scala           # Extension methods for streaming chat completions
```

**Supported Effect Systems:**
- **fs2**: `Stream[F, ChatChunkResponse]` with cats-effect
- **zio**: `ZStream[Any, OpenAIException, ChatChunkResponse]`
- **akka**: `Source[ChatChunkResponse, NotUsed]` (Scala 2.13 only)
- **pekko**: `Source[ChatChunkResponse, NotUsed]`
- **ox**: `Flow[ChatChunkResponse]` direct-style (Scala 3 only)

### Build Configuration (`project/`)
```
project/
├── Dependencies.scala       # Centralized version and dependency management
├── plugins.sbt            # sbt plugins configuration
└── build.properties       # sbt version
```

### Navigation Tips
- **Find API endpoint code**: Look in `core/src/main/scala/sttp/openai/requests/{api-category}/`
- **Model definitions**: Search for `ChatCompletionModel`, `EmbeddingModel`, etc. in respective request body files
- **Streaming implementation**: Check `streaming/{effect-system}/src/main/scala/`
- **Examples**: All runnable examples are in `examples/src/main/scala/examples/`
- **Tests**: Each module has tests in `{module}/src/test/` following same package structure

## Development Commands

### Build and Test
```bash
# Compile the project
sbt compile

# Run all tests (excluding integration tests)
sbt test

# Run integration tests (requires OPENAI_API_KEY)
./run-integration-tests.sh

# Compile documentation
sbt compileDocumentation

# Format code
sbt scalafmtAll
```

### Cross-platform Building
The project uses sbt-projectmatrix for cross-building:
- Scala 2.13.16 and Scala 3.3.6 support
- Core module supports both versions
- Streaming modules (fs2, zio, pekko, ox) have specific version requirements
- Akka module only supports Scala 2.13
- Ox module only supports Scala 3

### Testing Strategy
- Unit tests for all modules in `*/src/test`
- Integration tests that hit real OpenAI API (cost-efficient, minimal inputs)
- Integration tests automatically skip if `OPENAI_API_KEY` not set
- Use `sbt "testOnly *OpenAIIntegrationSpec"` for integration tests only

## Architecture

### Core Module Structure
- **OpenAI**: Raw sttp-client4 requests returning `Either[OpenAIException, A]`
- **OpenAISyncClient**: High-level synchronous client with blocking methods that may throw `OpenAIException`
- **Streaming Support**: Separate modules for different streaming libraries (fs2, zio, akka/pekko, ox)

### Key Components
- **Requests Package**: Contains all API endpoint definitions organized by OpenAI API categories
  - `completions.chat` - Chat completions API
  - `audio` - Speech synthesis, transcriptions, translations
  - `images` - Image generation, editing, variations
  - `embeddings` - Text embeddings
  - `files`, `assistants`, `threads` - OpenAI platform features
- **JSON Handling**: Uses uPickle with SnakePickle for snake_case conversion
- **Error Handling**: Comprehensive OpenAIException hierarchy for different API errors

### Streaming Architecture
Each streaming module provides:
- Extension methods for streaming chat completions
- Backend-specific implementations (HttpClientFs2Backend, etc.)
- Stream types: `Stream[F, ChatChunkResponse]` for fs2, `ZStream` for ZIO, etc.

### Client Implementations
- **Sync Client**: Uses DefaultSyncBackend, blocks on responses
- **Async Client**: Use OpenAI class with chosen backend (cats-effect, ZIO, etc.)
- **Custom Backends**: Support for any sttp backend through `.send(backend)`

## Model Management

The project includes automated scripts for updating OpenAI model definitions:

### Model Update Workflow
1. **Scrape Models**: `scala-cli model_update_scripts/scrape_models.scala`
   - Uses Playwright + Firefox to scrape OpenAI docs
   - Generates `models.json` with current model mappings
2. **Update Code**: `scala-cli model_update_scripts/update_code_with_new_models.scala --apply`
   - Updates Scala case objects with new models
   - Maintains alphabetical ordering
   - Only adds models (manual removal required)
3. **Format**: Run `sbt scalafmtAll` after updates

## Code Style

- **Formatting**: Uses Scalafmt with max column 140, Scala 3 dialect
- **Naming**: Snake case for JSON fields (handled by SnakePickle)
- **Imports**: SortImports rule applied, RedundantBraces/Parens removed
- **Case Objects**: Models defined as case objects extending sealed traits
- **Companion Values**: Some models maintain values sets for easy access

## Dependencies

- **sttp-client4**: HTTP client foundation (v4.0.11)
- **upickle/ujson**: JSON serialization (v4.3.2)
- **tapir**: API specification and JSON schema generation (v1.11.44)
- **scalatest**: Testing framework (v3.2.19)
- **Effect Libraries**: fs2, ZIO, Akka/Pekko Streams, Ox for streaming

## Examples

The `examples/` module contains runnable examples using scala-cli:
- Basic chat completion
- Streaming with different backends
- OpenAI-compatible APIs (Ollama, Grok)
- Structured outputs with JSON Schema
- Function calling

## CI/CD

- **GitHub Actions**: Uses SoftwareMill's shared workflows
- **Java 21**: Build and test environment
- **Scala Steward**: Automated dependency updates
- **Auto-merge**: For dependency PRs from softwaremill-ci
- **Publishing**: Automatic releases on version tags

## Integration Testing

Integration tests require a real OpenAI API key but are designed to be cost-efficient:
- Minimal inputs to reduce API costs
- Automatic skipping when API key unavailable
- 30-second timeouts
- Rate limiting handling
- See `INTEGRATION_TESTING.md` for detailed setup