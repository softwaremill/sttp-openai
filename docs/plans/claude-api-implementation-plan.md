# Claude API Integration Plan

## Overview
Implement native Claude API support for the sttp-openai library, focusing on the `/v1/messages` endpoint (equivalent to OpenAI's chat completions). This will be an **additive implementation** alongside existing OpenAI functionality, providing production-ready Claude API integration.

## Key Design Decisions
- **Additive approach**: Add Claude support without modifying existing OpenAI functionality
- **Minimal initial scope**: Focus only on messages API (most critical use case)
- **Consistent patterns**: Follow existing sttp-openai architecture and conventions
- **Full feature support**: Include sync/async operations and streaming
- **Reuse infrastructure**: Leverage existing JSON serialization and streaming modules

## Files to Create/Modify

### Core New Files
- **`core/src/main/scala/sttp/openai/Claude.scala`** - Raw Claude API requests (mirrors OpenAI.scala)
- **`core/src/main/scala/sttp/openai/ClaudeSyncClient.scala`** - High-level sync client (mirrors OpenAISyncClient.scala)
- **`core/src/main/scala/sttp/openai/requests/claude/ClaudeRequestBody.scala`** - Messages request models
- **`core/src/main/scala/sttp/openai/requests/claude/ClaudeResponseData.scala`** - Messages response models
- **`core/src/main/scala/sttp/openai/requests/claude/ClaudeChunkResponseData.scala`** - Streaming response models

### Files to Modify
- **`core/src/main/scala/sttp/openai/OpenAIExceptions.scala`** - Add Claude-specific exception types
- **`streaming/{fs2,zio,akka,pekko,ox}/src/main/scala/sttp/openai/streaming/{effect}/package.scala`** - Add Claude streaming support

## Key Functions to Implement

### Claude.scala
- `createMessage(request: ClaudeMessageRequest): Request[Either[OpenAIException, ClaudeMessageResponse], Any]` - Send message to Claude API
- `createMessageStream(request: ClaudeMessageRequest): Request[Either[OpenAIException, Source], Streams]` - Send streaming message request

### ClaudeSyncClient.scala
- `createMessage(request: ClaudeMessageRequest): ClaudeMessageResponse` - Synchronous message creation, throws exceptions on error
- `createMessageStream[S](request: ClaudeMessageRequest)(backend: StreamBackend[S]): Source` - Synchronous streaming wrapper

### Request/Response Models
- `ClaudeMessageRequest` case class - Wraps model, messages, max_tokens, temperature, etc.
- `ClaudeMessageResponse` case class - Contains id, content, role, stop_reason, usage
- `ClaudeStreamingChunk` case class - Individual streaming response chunks with event types

### Streaming Extensions (per module)
- `implicit class ClaudeStreamingExtensions` - Adds `.streamClaude()` method to backend types

## Test Coverage

### Core API Tests
- **`ClaudeMessageCreationSpec`** - Basic message request/response validation
- **`ClaudeAuthenticationSpec`** - Correct headers (x-api-key, anthropic-version) sent
- **`ClaudeErrorHandlingSpec`** - API error responses parsed correctly
- **`ClaudeModelSelectionSpec`** - Different Claude model variants work

### Streaming Tests
- **`ClaudeStreamingFs2Spec`** - fs2 Stream[F, ClaudeChunk] functionality
- **`ClaudeStreamingZIOSpec`** - ZIO ZStream streaming works correctly
- **`ClaudeStreamingAkkaSpec`** - Akka Source streaming implementation
- **`ClaudeStreamingPekkoSpec`** - Pekko Source streaming implementation
- **`ClaudeStreamingOxSpec`** - Ox Flow streaming for Scala 3

### Integration Tests
- **`ClaudeIntegrationSpec`** - Real API calls with ANTHROPIC_API_KEY
- **`ClaudeStreamingIntegrationSpec`** - End-to-end streaming with real API

## Implementation Details

### Claude API Differences from OpenAI
- **Base URL**: `https://api.anthropic.com/v1/messages` (vs OpenAI's `/chat/completions`)
- **Authentication**: `x-api-key` header (vs `Authorization: Bearer`)
- **Required headers**: `anthropic-version: 2023-06-01`, `content-type: application/json`
- **Request structure**: Similar to OpenAI but with Claude-specific field names
- **Response structure**: Different field names (`stop_reason` vs `finish_reason`, etc.)
- **Streaming**: Server-sent events with different event types

### Message Format Compatibility
- **User/Assistant roles**: Direct mapping from OpenAI format
- **System messages**: Separate field in Claude API (not in messages array)
- **Tool calling**: Claude has different tool calling format
- **Content types**: Text and image support similar to OpenAI

### Error Handling
- Claude API returns different error structures than OpenAI
- Need Claude-specific exception types in OpenAIExceptions.scala
- Maintain consistent error handling patterns with existing codebase

This implementation will provide a complete, production-ready Claude API client that follows sttp-openai patterns while supporting all major Scala effect systems and streaming libraries.