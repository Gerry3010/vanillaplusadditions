#!/bin/bash
# Build mod and start test server

echo "=== VanillaPlusAdditions Build & Test ==="

# Navigate to project root and build
cd ..
echo "Building mod..."
./gradlew build --no-daemon

# Check if build succeeded
if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
    cd test-server
    echo "Starting test server..."
    ./start-server.sh
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi