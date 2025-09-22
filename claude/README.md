# Claude API Module

This module provides native support for Anthropic's Claude API within the sttp-openai library.

## Features

- Native Claude API support (not OpenAI compatibility layer)
- Support for Claude's ContentBlock structure for messages
- Authentication via x-api-key and anthropic-version headers
- Messages API implementation
- Models API implementation
- Error handling with Claude-specific exceptions

## Usage

```scala
import sttp.ai.claude._
import sttp.client4._

val config = ClaudeConfig(
  apiKey = "your-api-key",
  anthropicVersion = "2023-06-01"
)

val client = ClaudeClient(config, DefaultSyncBackend())
```

## API Endpoints Supported

- Messages API (`/v1/messages`)
- Models API (`/v1/models`)

## Key Differences from OpenAI API

- Uses `ContentBlock` arrays instead of simple strings for message content
- System messages handled via `system` parameter, not role
- Different authentication headers (`x-api-key`, `anthropic-version`)
- Different tool calling structure
- Image support via ContentBlock with base64 encoding