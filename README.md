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

- **🤖 AI-Powered Conversations** - Contextual dialogue using Chutes AI, OpenRouter, or KoboldCpp
- **🎒 Inventory & Scavenging** - She picks up items, holds your gear, and gives it back when asked
- **🧠 Intelligent Interaction** - Ask "Do you have any food?" and she will check her inventory and share
- **🔄 Multiple AI Provider Support** - Choose between Cloud (Chutes/OpenRouter) or Local (KoboldCpp)
- **💬 Relationship Memory System** - Your girlfriend remembers your conversations
- **⚙️ Configurable AI Settings** - Temperature, model selection, custom prompts
- **🎨 Customizable System Prompts** - Edit personality via config file
- **🎭 Custom Skins** - Resource pack support for custom girlfriend textures
- **⚔️ Combat Partner** - Auto-equips weapons and fights by your side
- **💀 Knockout System** - Falls unconscious at 0 HP, respawns after 20 seconds

### ❤️ Original Features (CC0 Licensed - UltimateGamerMC)

- Companion entity that follows you
- Relationship system (0-100)
- Feeding and healing mechanics
- Following/waiting behavior
- Defending behavior

## 🎯 What Makes This Special

### Your Perfect Companion
- **Always There For You** - She follows you everywhere, stays by your side
- **Growing Relationship** - Build a real bond through care and conversation
- **Heartfelt Messages** - Receive genuine affection with romantic phrases
- **She Remembers You** - AI memory persists across sessions
- **Intelligent Conversations** - She understands context and responds naturally

### Living, Breathing Connection
- **Feed & Care** - Share meals to strengthen your bond
- **Protective Love** - She defends you from hostile mobs
- **Helpful Scavenger** - She picks up dropped items so you don't miss them
- **Smart Inventory** - Give her items to hold, or ask for them back via chat
- **Custom Identity** - Name her whatever you want
- **Custom Appearance** - Use resource packs for custom skins
- **Knockout & Respawn** - She can be knocked out and will respawn nearby

### AI-Powered Conversations
- **Dynamic Responses** - Context-aware dialogue using Chutes AI, OpenRouter, or KoboldCpp
- **Intent Analysis** - She understands when you are asking for items vs. just chatting
- **Multiple Providers** - Choose your preferred AI service (cloud or local)
- **Customizable Personality** - Edit the system prompt to change her behavior
- **Local Option** - Run AI completely offline with KoboldCpp

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

Press `G` to open the AI configuration screen to set up your API keys and preferences, or edit `girlfriend-mod.json` manually:

```json
{
  "aiProvider": "KOBOLDCPP",
  "chutesApiKey": "your-chutes-api-key",
  "chutesModelName": "Qwen/Qwen2.5-VL-72B-Instruct-TEE",
  "openRouterApiKey": "your-openrouter-api-key",
  "openRouterModelName": "anthropic/claude-sonnet-4-20250514",
  "koboldCppUrl": "http://localhost:5001",
  "koboldCppModel": "kcpp",
  "koboldCppUseChatCompletions": true,
  "customName": "YourGirlfriendsName",
  "temperature": 0.85,
  "minP": 0.05,
  "maxHistoryTokens": 8192,
  "enableAI": true,
  "customTexturePath": "girlfriend-mod:textures/entity/girlfriend.png"
}
```

## 🤖 AI Provider Options

### Option 1: Chutes AI (Cloud)
- Easy to set up - just get an API key
- Powerful cloud models
- Requires internet connection

**Get API key:** https://chutes.ai

### Option 2: OpenRouter (Cloud)
- Access to multiple models (Claude, GPT, etc.)
- Pay-per-use pricing
- Requires internet connection

**Get API key:** https://openrouter.ai

### Option 3: KoboldCpp (Local) ⭐
- **Runs completely offline** - no data leaves your machine
- **No API key required**
- **Free to use**
- Supports various GGUF models
- Perfect for privacy-conscious users

**Download:** https://github.com/LostRuins/koboldcpp

**Setup:**
1. Download and run KoboldCpp
2. Load your preferred GGUF model
3. Set URL to `http://localhost:5001` (or your custom port)
4. Choose API format (OpenAI Chat recommended)

## 🎮 Controls & Interaction

| Action | Control |
|--------|---------|
| Feed | Right-click with food |
| Give Item | Right-click with item (non-food) |
| Toggle Follow/Wait | Sneak + Right-click |
| Open AI Config | Press `G` |

### Chat Interaction
Simply type in chat to talk to her!
- **Chat:** "How are you doing?"
- **Request Items:** "Can I have that diamond?" or "Give me some food"

## 💬 Commands

| Command | Description |
|---------|-------------|
| `/girlfriend summon <player>` | Summon a girlfriend |
| `/girlfriend relationship <player> <0-100>` | Set relationship level |
| `/girlfriend list <player>` | Count girlfriends |
| `/girlfriend dismiss <player>` | Dismiss a girlfriend |
| `/girlfriend dismiss all` | Dismiss all girlfriends (OP) |
| `/girlfriend dismiss nearby` | Dismiss nearby girlfriends (OP) |
| `/girlfriend reloadprompt` | Reload system prompt |
| `/girlfriend config` | Info on how to open config |

## 🎭 Custom Skins

You can customize your girlfriend's appearance using resource packs! There are two methods:

### Method 1: Resource Pack (Recommended)

Create a resource pack with the following structure:

```
📁 my-skin-resource-pack/
├── 📄 pack.mcmeta
├── 📄 pack.png (optional)
└── 📁 assets/
    └── 📁 girlfriend-mod/
        └── 📁 textures/
            └── 📁 entity/
                └── 📄 girlfriend.png
```

**pack.mcmeta:**
```json
{
    "pack": {
        "description": "Custom GF Skin",
        "min_format": 69,
        "max_format": 69
    }
}
```

Then:
1. Add the resource pack to your Minecraft client
2. Open AI Config (press `G`)
3. Set "Skin Texture Path" to: `girlfriend-mod:textures/entity/girlfriend.png`
4. Save and restart if needed

### Method 2: Direct Path (Advanced)

You can also use external texture paths:
- `girlfriend-mod:textures/entity/girlfriend.png` (default)
- `minecraft:textures/entity/steve.png` (use Steve's skin)
- `custom-mod:textures/entity/my_skin.png` (custom namespace)

## 📁 File Structure

```
girlfriend-mod/
├── src/main/java/com/beckytidus/girlfriendmod/
│   ├── GirlfriendMod.java           # Main mod class
│   ├── GirlfriendModClient.java     # Client initialization
│   ├── ai/
│   │   ├── AIClientManager.java     # Unified AI client router
│   │   ├── ChutesClient.java        # Chutes AI API integration
│   │   ├── OpenRouterClient.java    # OpenRouter API integration
│   │   ├── KoboldCppClient.java     # KoboldCpp API integration (NEW)
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
│   │   ├── ChatEventHandler.java    # Chat message handler (NEW)
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
│           └── GirlFriendEntityRenderer.java  # Custom skin rendering
└── src/main/resources/
    ├── fabric.mod.json
    ├── girlfriend-mod.mixins.json
    ├── assets/girlfriend-mod/
    │   ├── lang/en_us.json
    │   ├── textures/
    │   │   └── entity/
    │   │       └── girlfriend.png   # Default skin
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
- `{context}` - Current environment data (inventory, health, location)

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
- **KoboldCpp** - Local AI provider ([Terms](https://github.com/LostRuins/koboldcpp))
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