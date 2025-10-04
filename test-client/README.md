# VanillaPlusAdditions Test Client

This directory contains a test client environment for testing your VanillaPlusAdditions mod.

## Setup Complete

The NeoForge client installer has been run and the NeoForge profile is now available in your Minecraft launcher. Your mod JAR has been automatically placed in the `mods/` directory.

## How to Test Your Mod

### Option 1: Use Minecraft Launcher
1. Open the Minecraft launcher
2. Select the "NeoForge 21.0.167" profile from the dropdown
3. Launch the game
4. Create a new world or join your test server at `localhost:25565`

### Option 2: Copy to Main Minecraft Directory
```bash
cp mods/vanillaplusadditions-1.0.0.jar ~/.minecraft/mods/
```

## Testing the Hostile Zombified Piglins Module

To test your mod's functionality:

1. Create a new Creative world
2. Go to the Nether
3. Spawn zombified piglins using spawn eggs
4. Switch to Survival mode (`/gamemode survival`)
5. Approach the zombified piglins - they should immediately become hostile instead of remaining neutral

## Files in This Directory

- `mods/` - Contains your built mod JAR
- `launcher_profiles.json` - Launcher configuration
- `options.txt` - Basic game settings optimized for testing
- `launch-client.sh` - Information script about testing setup
- `README.md` - This file

## Updating Your Mod

When you make changes to your mod:

1. Run `./gradlew build` in the main project directory
2. Copy the new JAR: `cp ../build/libs/vanillaplusadditions-1.0.0.jar mods/`
3. Restart Minecraft to load the updated mod

## Troubleshooting

- Check game logs for mod loading confirmation and any errors
- Make sure Java 21 is being used
- Verify that the NeoForge profile appears in the launcher
- Ensure the mod JAR is in the `mods/` directory

## Log Files

When testing, check these locations for logs:
- Game logs: This directory after running the game
- Build logs: `../build/` directory
- Launcher logs: `~/.minecraft/logs/`