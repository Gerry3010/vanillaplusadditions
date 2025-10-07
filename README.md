# VanillaPlusAdditions

A Minecraft NeoForge mod that enhances vanilla gameplay with useful additions while maintaining the original feel.

> 🤖 **AI Collaboration Notice**: This project was developed in collaboration with the Warp AI assistant (powered by Claude 3.5 Sonnet). The AI helped with code implementation, documentation, and project structure. While the core ideas and direction came from human creativity, the AI's assistance made this project more robust and feature-complete. We believe in transparency about AI usage while celebrating the potential of human-AI collaboration in software development.

## 🎯 Features

### 🔥 Hostile Zombified Piglins
- Makes zombified piglins always hostile in the Nether
- Configurable detection range and anger duration
- Smart targeting system with player switching

### 💀 Wither Skeleton Enforcer
- Prevents normal skeletons from spawning in the Nether
- Optionally replaces them with Wither Skeletons
- Server-wide broadcast messages for blocked spawns

### ✨ MobGlow Command
- Make specific mob types glow for easier tracking
- Configurable duration (including infinite)
- Clear glow effects by type or all at once
- Perfect for server administration and debugging

## 🔧 Configuration

Each module has its own configuration options. See our detailed guides:
- [Module Configuration Guide](MODULE_CONFIG_GUIDE.md)
- [Debug Logging Configuration](DEBUG_LOGGING_CONFIG.md)
- [MobGlow Command Guide](MOBGLOW_MODULE_GUIDE.md)

## 🚀 Installation

1. Download the latest version from [Releases](https://github.com/Gerry3010/vanillaplusadditions/releases)
2. Install NeoForge for Minecraft 1.21
3. Place the jar file in your mods folder
4. Start Minecraft and enjoy!

## 🔨 Development

### Prerequisites
- JDK 21
- Gradle 8.4+
- Git

### Setup
```bash
# Clone the repository
git clone https://github.com/Gerry3010/vanillaplusadditions.git
cd vanillaplusadditions

# Setup development environment
./gradlew build
```

### Test Environments
The project includes test server and client setups:
```bash
# Test server
cd test-server
./build-and-test.sh

# Test client
cd test-client
./launch-client.sh
```

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) first.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Credits

- **Developer**: Gerald Hofbauer
- **AI Assistant**: Warp AI (Claude 3.5 Sonnet)
- **Framework**: [NeoForge](https://neoforged.net/)

## 📚 Documentation

- [Module System Overview](MODULE_SYSTEM.md)
- [Configuration System](CONFIGURATION_SYSTEM_SUMMARY.md)
- [Testing Guide](TESTING.md)

## 🐛 Debug Logging

VanillaPlusAdditions includes a sophisticated debug logging system:
- Global and per-module control
- Detailed log messages for troubleshooting
- See [Debug Logging Guide](DEBUG_LOGGING_CONFIG.md)

## 🔗 Links

- [GitHub Repository](https://github.com/Gerry3010/vanillaplusadditions)
- [Issue Tracker](https://github.com/Gerry3010/vanillaplusadditions/issues)
- [NeoForge](https://neoforged.net/)

## 💬 About AI Assistance

This project demonstrates the potential of human-AI collaboration in software development. The AI assistant helped with:

- Code implementation
- Documentation writing
- Project structure
- CI/CD setup
- Testing frameworks
- Bug fixes

While the AI provided technical assistance, all creative decisions, feature ideas, and project direction came from human input. We believe this transparency about AI usage is important for the open-source community.