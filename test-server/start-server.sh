#!/bin/bash
# VanillaPlusAdditions Test Server Startup Script

echo "=== VanillaPlusAdditions Test Server ==="
echo "Starting Minecraft 1.21.1 NeoForge test server..."

# Copy the latest mod build if it exists
MOD_JAR="../build/libs/vanillaplusadditions-1.0.0.jar"
if [ -f "$MOD_JAR" ]; then
    echo "✓ Found mod jar, copying to mods folder..."
    cp "$MOD_JAR" mods/
    echo "✓ VanillaPlusAdditions mod loaded"
else
    echo "⚠ Warning: Mod jar not found at $MOD_JAR"
    echo "  Run './gradlew build' in the main project directory first"
fi

echo "Starting server..."
echo "==============================================="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java not found. Please install Java 21."
    exit 1
fi

# Start the server using the generated run.sh
if [ -f "run.sh" ]; then
    chmod +x run.sh
    ./run.sh
else
    echo "Error: run.sh not found. Please run the NeoForge installer first."
    exit 1
fi