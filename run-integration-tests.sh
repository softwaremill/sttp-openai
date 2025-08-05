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
    echo "❌ Error: OPENAI_API_KEY is required"
    echo ""
    echo "Usage:"
    echo "  1. Set environment variable: export OPENAI_API_KEY=your-key-here"
    echo "  2. Or pass as argument: ./run-integration-tests.sh your-key-here"
    echo ""
    echo "To skip integration tests (for CI/CD without API key):"
    echo "  sbt \"testOnly *OpenAIIntegrationSpec\" -Dtest.integration.skip=true"
    exit 1
fi

echo "🧪 Running sttp-openai integration tests..."
echo "📊 Estimated cost: ~$0.002 (less than 1 cent)"
echo ""

# Run the integration tests
sbt "testOnly *OpenAIIntegrationSpec"

echo ""
echo "✅ Integration tests completed!"
echo ""
echo "Tests covered:"
echo "  • Models API (FREE) - List and retrieve models"
echo "  • Moderations API (FREE) - Content moderation"  
echo "  • Embeddings API (~$0.0001) - Text embeddings"
echo "  • Chat Completions API (~$0.001) - Chat with minimal tokens"
echo "  • Error handling - Authentication and rate limiting"
echo "  • Client customization - Request modification"