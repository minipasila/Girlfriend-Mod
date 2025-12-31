# Girlfriend Mod

<div align="center">

![Girlfriend Mod](https://img.shields.io/badge/Minecraft-1.21.9-green?style=for-the-badge)
![Fabric](https://img.shields.io/badge/Fabric%20API-0.134.0-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-CC0--1.0/GPL--2.0-orange?style=for-the-badge)

**An interactive Minecraft companion mod with AI-powered conversations**

*Never adventure alone again with a loyal companion who genuinely cares about you*

</div>

## 📖 About This Mod

This is a fork and expansion of the original [Girlfriend Mod](https://github.com/UltimateGamerMC/Girlfriend-Mod) by UltimateGamerMC, with the following additions and modifications:

### ✨ New Features (minipasila's Additions - GPLv2 Licensed)

- **🤖 AI-Powered Conversations** - Using Chutes AI API or OpenRouter for dynamic, contextual dialogue
- **🔄 Multiple AI Provider Support** - Choose between Chutes AI (default) or OpenRouter
- **💬 Relationship Memory System** - Your girlfriend remembers your conversations
- **⚙️ Configurable AI Settings** - Temperature, model selection, custom prompts
- **🎨 Customizable System Prompts** - Edit personality via config file
- **🔧 Client-Side GUI** - Easy configuration without editing JSON files
- **📝 Memory Management** - Clear and manage conversation history

### ❤️ Original Features (CC0 Licensed - UltimateGamerMC)

- Companion entity that follows you
- Relationship system (0-100)
- Gift system
- Feeding and healing mechanics
- Basic romantic dialogue
- Following/waiting behavior
- Defending behavior

## 🎯 What Makes This Special

### Your Perfect Companion
- **Always There For You** - She follows you everywhere, stays by your side
- **Growing Relationship** - Build a real bond through care and conversation
- **Heartfelt Messages** - Receive genuine affection with romantic phrases
- **She Remembers You** - AI memory persists across sessions

### Living, Breathing Connection
- **Feed & Care** - Share meals to strengthen your bond
- **Protective Love** - She defends you from hostile mobs
- **Surprise Gifts** - Receive diamonds, emeralds, and rare treasures
- **Custom Identity** - Name her whatever you want

### AI-Powered Conversations
- **Dynamic Responses** - Context-aware dialogue using Chutes AI or OpenRouter
- **Memory & Learning** - She remembers your conversations
- **Multiple Providers** - Choose your preferred AI service
- **Customizable Personality** - Edit the system prompt to change her behavior
- **Multiple Model Support** - Choose your preferred AI model

## 🚀 Getting Started

### Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.9
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) 0.134.0+1.21.9
3. Download the latest `girlfriend-mod-X.X.X.jar` from [Releases](../../releases)
4. Place the JAR in your `mods/` folder
5. Launch Minecraft with the Fabric profile

### Quick Start

**Option 1: Craft the Summoner**
```
Pattern:
  D = Diamond
  H = Heart of the Sea

    D
  D H D   =   Girlfriend Summoner
    D
```

**Option 2: Use Command**
```
/girlfriend summon <player>
```

### AI Configuration

Press `G` to open the AI configuration screen, or edit `girlfriend-mod.json` manually:

```json
{
  "aiProvider": "CHUTES",
  "chutesApiKey": "your-chutes-api-key",
  "chutesModelName": "Qwen/Qwen2.5-VL-72B-Instruct-TEE",
  "openRouterApiKey": "your-openrouter-api-key",
  "openRouterModelName": "anthropic/claude-sonnet-4-20250514",
  "customName": "YourGirlfriendsName",
  "temperature": 0.85,
  "minP": 0.05,
  "maxHistoryTokens": 8192,
  "enableAI": true
}
```

**Get your free API key at:**
- [Chutes AI](https://chutes.ai)
- [OpenRouter](https://openrouter.ai)

## 🎮 Controls

| Action | Control |
|--------|---------|
| Feed | Right-click with food |
| Toggle Follow/Wait | Sneak + Right-click |
| Open AI Config | Press `G` |

## 💬 Commands

| Command | Description |
|---------|-------------|
| `/girlfriend summon <player>` | Summon a girlfriend |
| `/girlfriend relationship <player> <0-100>` | Set relationship level |
| `/girlfriend list <player>` | Count girlfriends |
| `/girlfriend reloadprompt` | Reload system prompt |

## 📁 File Structure

```
girlfriend-mod/
├── src/main/java/com/beckytidus/girlfriendmod/
│   ├── GirlfriendMod.java           # Main mod class
│   ├── GirlfriendModClient.java     # Client initialization
│   ├── ai/
│   │   ├── AIClientManager.java     # Unified AI client router
│   │   ├── ChutesClient.java        # Chutes AI API integration
│   │   ├── OpenRouterClient.java    # OpenRouter API integration (NEW)
│   │   └── ConversationManager.java # Memory system
│   ├── entity/
│   │   └── GirlFriendEntity.java    # Core entity
│   ├── registry/
│   │   ├── EntityRegistry.java      # Entity registration
│   │   └── ItemRegistry.java        # Item registration
│   ├── item/
│   │   └── GirlFriendSummonerItem.java
│   ├── command/
│   │   └── GirlFriendCommand.java
│   ├── interaction/
│   │   ├── EntityInteractionHandler.java
│   │   └── ItemUseHandler.java
│   ├── event/
│   │   ├── ChatEventHandler.java
│   │   └── EntityAttributeHandler.java
│   ├── gui/
│   │   └── AIConfigScreen.java      # Config GUI with provider selection
│   ├── config/
│   │   └── ModConfig.java           # Config with AI provider support
│   ├── network/
│   │   └── ModNetwork.java          # Sync config/memory
│   └── client/
│       ├── KeyInputHandler.java
│       └── render/
│           └── GirlFriendEntityRenderer.java
└── src/main/resources/
    ├── fabric.mod.json
    ├── girlfriend-mod.mixins.json
    ├── assets/girlfriend-mod/
    │   ├── lang/en_us.json
    │   ├── textures/
    │   └── system-prompt.txt        # AI personality
    └── data/girlfriend-mod/recipes/
        └── girlfriend_summoner.json
```

## 🔧 Configuration Files

| File | Purpose |
|------|---------|
| `girlfriend-mod.json` | AI and mod settings (including provider selection) |
| `girlfriend-mod/system-prompt.txt` | AI personality |
| `girlfriend-mod/memories/*.json` | Conversation history |

## 📝 Customizing the AI Personality

Edit `system-prompt.txt` in your config folder to customize your girlfriend's personality:

```txt
roleplay as {name}, a gentle and soft-spoken ai girlfriend...
```

Available placeholders:
- `{name}` - Your girlfriend's name
- `{context}` - Current environment data

## 🤝 License & Attribution

### Original Project (CC0 1.0 Universal)

This mod is based on the [Girlfriend Mod](https://github.com/UltimateGamerMC/Girlfriend-Mod) by **UltimateGamerMC**, licensed under CC0 1.0 Universal (public domain dedication).

### Additions & Modifications (GPLv2)

All additions, modifications, and new features implemented by **minipasila** (https://github.com/minipasila) are licensed under the **GNU General Public License v2.0**.

This means:
- You are free to use, modify, and distribute this code
- If you distribute modified versions, you must also license them under GPLv2
- The original CC0-licensed code remains in the public domain
- Combined work is dual-licensed under CC0 (original) and GPLv2 (additions)

### Third-Party Dependencies
- **Chutes AI** - For AI conversation generation ([Terms](https://chutes.ai/terms))
- **OpenRouter** - Alternative AI provider ([Terms](https://openrouter.ai/terms))
- **Fabric API** - Minecraft modding API ([License](https://github.com/FabricMC/fabric/blob/master/LICENSE))
- **Gson** - JSON serialization ([License](https://github.com/google/gson/blob/master/LICENSE))

## ⚠️ Disclaimer

This is a work of fiction. The Girlfriend Mod is a fictional companion entity for entertainment purposes in Minecraft. It does not represent a real relationship and should not replace real human connections.

**Because everyone deserves someone who's always happy to see you.** ❤️

---

<div align="center">

**Made with ❤️ by minipasila**

[GitHub](https://github.com/minipasila/Girlfriend-Mod) • [Issues](../../issues) • [Discussions](../../discussions)

</div>