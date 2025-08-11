#!/bin/bash

# Script to run integration tests for sttp-openai library
# Usage: 
#   ./run-integration-tests.sh [your-openai-api-key]
# 
# Or set the OPENAI_API_KEY environment variable and run:
#   ./run-integration-tests.sh

set -e

if [ "$#" -eq 1 ]; then
    export OPENAI_API_KEY="$1"
fi

if [ -z "${OPENAI_API_KEY}" ]; then
    echo "⚠️  Warning: OPENAI_API_KEY is not set"
    echo ""
    echo "Usage:"
    echo "  1. Set environment variable: export OPENAI_API_KEY=your-key-here"
    echo "  2. Or pass as argument: ./run-integration-tests.sh your-key-here"
    echo ""
    echo "📝 Note: Without API key, all integration tests will be skipped (not failed)"
    echo "This is CI/CD friendly behavior."
    echo ""
    echo "Proceeding with tests (they will be skipped)..."
    echo ""
fi

echo "🧪 Running sttp-openai integration tests..."
if [ -n "${OPENAI_API_KEY}" ]; then
    echo "📊 Estimated cost: ~$0.003 (less than 1 cent)"
else
    echo "📊 Cost: FREE (tests will be skipped)"
fi
echo ""

# Run the integration tests
sbt "testOnly *OpenAIIntegrationSpec"

echo ""
if [ -n "${OPENAI_API_KEY}" ]; then
    echo "✅ Integration tests completed!"
    echo ""
    echo "Tests covered:"
    echo "  • Models API (FREE) - List and retrieve models"
    echo "  • Moderations API (FREE) - Content moderation"  
    echo "  • Embeddings API (~$0.0001) - Text embeddings"
    echo "  • Chat Completions API (~$0.001) - Chat with minimal tokens"
    echo "  • Responses API (~$0.001) - Complete lifecycle (create/retrieve/delete)"
    echo "  • Error handling - Authentication and rate limiting"
    echo "  • Client customization - Request modification"
else
    echo "✅ Integration tests completed (all tests skipped)!"
    echo ""
    echo "Tests that would be covered with API key:"
    echo "  • Models API (FREE) - List and retrieve models"
    echo "  • Moderations API (FREE) - Content moderation"  
    echo "  • Embeddings API (~$0.0001) - Text embeddings"
    echo "  • Chat Completions API (~$0.001) - Chat with minimal tokens"
    echo "  • Responses API (~$0.001) - Complete lifecycle (create/retrieve/delete)"
    echo "  • Error handling - Authentication and rate limiting"
    echo "  • Client customization - Request modification"
fi