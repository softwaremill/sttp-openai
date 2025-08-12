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
    echo ""
    echo "Proceeding with tests (they will be skipped)..."
    echo ""
fi

echo "🧪 Running sttp-openai integration tests..."

# Run the integration tests
sbt "testOnly *OpenAIIntegrationSpec"

echo ""
if [ -n "${OPENAI_API_KEY}" ]; then
    echo "✅ Integration tests completed!"
else
    echo "✅ Integration tests skipped (no API key provided)"
fi