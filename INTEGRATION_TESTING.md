# Integration Testing Guide

This document explains how to run integration tests for the sttp-openai library against the real OpenAI API.

## Overview

The integration tests are designed to be **cost-efficient** while providing comprehensive coverage of the library's functionality. They focus on:

- **Free endpoints**: Models API, Moderations API
- **Very cheap endpoints**: Embeddings API (minimal text)
- **Low-cost endpoints**: Chat Completions API (GPT-4o-mini with token limits)

## Running Integration Tests

### Prerequisites

1. **OpenAI API Key**: You need a valid OpenAI API key with sufficient credits
2. **Internet Connection**: Tests make real API calls to OpenAI

### Setup

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=your-api-key-here
```

### Running Tests

#### Run all integration tests:
```bash
sbt "testOnly *OpenAIIntegrationSpec"
```

#### Run specific test categories:
```bash
# Run only integration-tagged tests
sbt "testOnly *OpenAIIntegrationSpec -- -n integration"
```

#### Skip integration tests (when no API key is available):
If `OPENAI_API_KEY` is not set, all tests will be automatically skipped (not failed).
This makes the tests CI/CD friendly.

## Test Coverage

### 1. Models API (`getModels`, `retrieveModel`)
- **Cost**: FREE
- **Coverage**: Basic connectivity, authentication, response parsing
- **What it tests**:
  - Lists available models
  - Retrieves specific model details
  - Validates response structure

### 2. Moderations API (`createModeration`)
- **Cost**: FREE
- **Coverage**: Content moderation functionality
- **What it tests**:
  - Moderates harmless content (should not be flagged)
  - Moderates inappropriate content (tests structure)
  - Validates response format and fields

### 3. Embeddings API (`createEmbeddings`)
- **Cost**: VERY LOW (~$0.0001 per test run)
- **Coverage**: Vector embedding generation
- **What it tests**:
  - Creates embeddings for minimal text ("test")
  - Uses `text-embedding-3-small` (cheapest model)
  - Validates embedding dimensions and usage statistics

### 4. Chat Completions API (`createChatCompletion`)
- **Cost**: LOW (~$0.001 per test run)
- **Coverage**: Core chat functionality
- **What it tests**:
  - Creates chat completion with minimal prompt ("Hi")
  - Uses `gpt-4o-mini` (cheapest GPT model)
  - Limits to 5 output tokens maximum
  - Validates response structure and content

### 5. Error Handling
- **Cost**: FREE (invalid requests)
- **Coverage**: Exception handling and error responses
- **What it tests**:
  - Authentication errors with invalid API key
  - Rate limiting behavior
  - Proper exception types and messages

### 6. Responses API (`createModelResponse`, `getModelResponse`, `listResponsesInputItems`, `deleteModelResponse`)
- **Cost**: LOW (~$0.001 per test run)
- **Coverage**: Complete Responses API lifecycle
- **What it tests**:
  - Creates response with minimal input ("Hi")
  - Uses `gpt-4o-mini` (cheapest model)
  - Retrieves response by ID (stateful API)
  - Lists input items to validate conversation state
  - Deletes response for cleanup
  - Validates response structure and usage statistics

### 7. Client Customization
- **Cost**: FREE (uses Models API)
- **Coverage**: Request customization functionality
- **What it tests**:
  - Custom headers injection
  - Request modification pipeline
  - Client configuration

## Cost Estimation

**Total estimated cost per complete test run**: ~$0.003 (less than 1 cent)

- Models API: $0.000 (free)
- Moderations API: $0.000 (free)
- Embeddings API: ~$0.0001 (1 token)
- Chat Completions: ~$0.001 (minimal tokens)
- Responses API: ~$0.001 (minimal tokens)
- Error handling: $0.000 (failed requests)

## Test Design Principles

### 1. **Minimal Token Usage**
- Use shortest possible prompts
- Set strict token limits (`maxTokens = 5`)
- Choose cheapest available models

### 2. **Free Endpoints First**
- Test authentication and basic connectivity with free endpoints
- Only use paid endpoints for features that require them

### 3. **Real API Behavior**
- Tests actual HTTP requests/responses
- Validates real JSON parsing
- Tests actual error conditions

### 4. **CI/CD Friendly**
- Automatically skipped when API key is not available (no failures)
- Clear messages when tests are skipped
- Fast execution (< 30 seconds total)

## Troubleshooting

### "OPENAI_API_KEY not defined - skipping integration test"
This is normal behavior when no API key is set. To run tests:
Set your API key: `export OPENAI_API_KEY=your-key`

### Rate limiting errors
The tests include rate limiting handling, but if you encounter issues:
- Wait a few minutes between test runs
- Ensure your API key has sufficient quota
- Check your OpenAI account usage limits

### Authentication errors
- Verify your API key is correct and active
- Check that your OpenAI account has sufficient credits
- Ensure the key has necessary permissions

### Network timeouts
- Check internet connectivity
- Verify OpenAI API is accessible from your network
- Tests have 30-second timeout configured

## Adding New Integration Tests

When adding new integration tests, follow these guidelines:

1. **Prioritize free/cheap endpoints**
2. **Use minimal inputs** to reduce costs
3. **Set strict limits** on output tokens
4. **Tag tests** with `IntegrationTest`

Example:
```scala
"New API endpoint" should "work correctly" taggedAs IntegrationTest in {
  withClient { client =>
    //given
    val minimalInput = createMinimalInput()
    
    //when
    val result = client.newEndpoint(minimalInput)
    
    //then
    result should not be null
    validateResult(result)
  }
}

private def createMinimalInput(): RequestType = {
  // Create minimal input to reduce API costs
}

private def validateResult(result: ResponseType): Unit = {
  // Validate structure and content
}
```
