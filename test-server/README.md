# VanillaPlusAdditions Test Server

This is a local test server for developing and testing the VanillaPlusAdditions mod.

## Quick Start

### Option 1: Build and Test (Recommended)
```bash
./build-and-test.sh
```
This will build the mod and start the server with the latest changes.

### Option 2: Start Server Only
```bash
./start-server.sh
```
This will copy the latest mod build (if available) and start the server.

## Server Configuration

- **Minecraft Version**: 1.21.1
- **Mod Loader**: NeoForge 21.0.167
- **Game Mode**: Creative (for easy testing)
- **Difficulty**: Easy
- **Port**: 25565 (localhost only)
- **Max Players**: 5

## Testing Your Mod

1. **Build the mod**: Run `./gradlew build` in the main project directory
2. **Start the server**: Use one of the startup scripts above
3. **Connect**: Connect to `localhost:25565` with Minecraft 1.21.1 + NeoForge
4. **Test features**: 
   - Go to the Nether to test hostile zombified piglins (if enabled)
   - Check the creative tabs for new items from EnhancedToolsModule
   - Test other module features

## Module Testing

The test server will load your mod with all registered modules:

- **EnhancedToolsModule** ✅ (Enabled by default)
- **HostileZombifiedPiglinsModule** ⚠️ (Disabled by default - enable in config)
- **ImprovedStorageModule** ⚠️ (Disabled by default)
- **QualityOfLifeModule** ✅ (Enabled by default)

To enable disabled modules, you would need to modify the configuration (future feature).

## Files & Directories

- `server.properties` - Server configuration
- `eula.txt` - Minecraft EULA acceptance
- `start-server.sh` - Convenient server startup script
- `build-and-test.sh` - Build mod and start server
- `mods/` - Mod files (auto-populated from build)
- `world/` - Test world data (gitignored)
- `logs/` - Server logs (gitignored)

## Troubleshooting

**Server won't start:**
- Ensure Java 21 is installed and available in PATH
- Check that the mod built successfully (`./gradlew build`)

**Mod not loading:**
- Verify the mod jar exists in `build/libs/`
- Check server logs for mod loading errors
- Ensure NeoForge version matches (21.0.167)

**Connection refused:**
- Server only accepts connections from localhost (127.0.0.1)
- Port 25565 must be available
- Use Minecraft 1.21.1 with NeoForge client

## Development Workflow

1. Make changes to your mod code
2. Run `./build-and-test.sh` to build and start server
3. Connect and test your changes
4. Stop server (Ctrl+C)
5. Repeat as needed

The server will automatically copy the latest mod build each time you start it.