# Girlfriend Mod

An interactive Minecraft mod that adds a customizable girlfriend entity companion with relationship mechanics, inventory management, AI-powered conversations, and smart interactions.

## Features

### Core Features
- **Girlfriend Entity** - Interactive companion that follows you
- **Relationship System** - Level 0-100 affects behavior
- **Inventory System** - 36-slot inventory; she picks up items and holds your gear
- **Feeding System** - Right-click to feed food items and increase relationship
- **Following Behavior** - Toggle follow/wait with sneak+right-click
- **Knockout System** - Falls unconscious at 0 HP, respawns after 20 seconds

### AI Features (Enhanced)
- **Dynamic Conversations** - Contextual dialogue using Chutes AI, OpenRouter, or KoboldCpp
- **Smart Requests** - Ask her for items via chat (e.g., "Give me the diamond") and she shares from her inventory
- **Multiple AI Providers** - Choose between cloud services or local AI
  - **Chutes AI** (cloud)
  - **OpenRouter** (cloud)
  - **KoboldCpp** (local/offline)
- **Memory System** - Remembers your conversations across sessions
- **Customizable Personality** - Edit system prompt for different behavior
- **Context Awareness** - Knows time of day, health status, inventory contents

### Combat Features
- **Auto-Weapon Selection** - Automatically equips best weapon (sword or bow)
- **Ranged Combat** - Uses bow with arrows from inventory
- **Owner Defense** - Attacks entities threatening the owner
- **Kill Reactions** - AI comments on defeating enemies

### Custom Skins
- **Resource Pack Support** - Create custom skins using standard Minecraft resource packs
- **GUI Configuration** - Set texture path directly in the AI Config screen

### Commands
- `/girlfriend summon <player>` - Summon a girlfriend
- `/girlfriend relationship <player> <0-100>` - Set relationship level
- `/girlfriend list <player>` - Count girlfriends
- `/girlfriend dismiss <player>` - Dismiss a girlfriend
- `/girlfriend dismiss all` - Dismiss all girlfriends (OP)
- `/girlfriend dismiss nearby` - Dismiss nearby girlfriends (OP)
- `/girlfriend reloadprompt` - Reload system prompt
- `/girlfriend config` - Info on how to open config

## Installation

1. Install Fabric Loader for Minecraft 1.21.9
2. Install Fabric API 0.134.0+1.21.9
3. Place `girlfriend-mod-X.X.X.jar` in your `mods/` folder

## Usage

1. **Craft the Summoner Item:**
   ```
   D = Diamond
   H = Heart of the Sea

   Pattern:
     D
   D H D   =   Girlfriend Summoner
     D
   ```

2. **Right-click** with the item to summon a girlfriend
3. **Right-click** to feed her food or give her items
4. **Sneak + Right-click** to toggle between follow and wait modes
5. **Press G** to configure AI settings
6. **Chat** with her by typing in standard Minecraft chat!

## AI Provider Setup

### Option 1: Chutes AI (Cloud)
1. Get a free API key from [chutes.ai](https://chutes.ai)
2. Open AI Config (press `G`)
3. Select "Chutes AI" as provider
4. Enter your API key
5. Start chatting with your girlfriend!

### Option 2: OpenRouter (Cloud)
1. Get an API key from [openrouter.ai](https://openrouter.ai)
2. Open AI Config (press `G`)
3. Select "OpenRouter" as provider
4. Enter your API key
5. Choose your preferred model (e.g., claude-sonnet-4-20250514)
6. Start chatting with your girlfriend!

### Option 3: KoboldCpp (Local)
1. Download [KoboldCpp](https://github.com/LostRuins/koboldcpp)
2. Run KoboldCpp and load a GGUF model
3. Open AI Config (press `G`)
4. Select "KoboldCpp (Local)" as provider
5. Set URL to `http://localhost:5001` (or your custom port)
6. Choose API format (OpenAI Chat recommended)
7. Start chatting with your girlfriend - completely offline!

## Custom Skins

### Creating a Custom Skin Resource Pack

1. Create a folder for your resource pack
2. Add `pack.mcmeta`:
   ```json
   {
       "pack": {
           "description": "Custom GF Skin",
           "min_format": 69,
           "max_format": 69
       }
   }
   ```
3. Add your skin texture at: `assets/girlfriend-mod/textures/entity/girlfriend.png`
4. Add the pack to Minecraft via the Resource Pack menu
5. Open AI Config (press G) and set "Skin Texture Path" to: `girlfriend-mod:textures/entity/girlfriend.png`

### Alternative Skin Paths

You can use various texture path formats:
- `girlfriend-mod:textures/entity/girlfriend.png` (default)
- `minecraft:textures/entity/steve.png` (Steve's skin)
- `your-mod:textures/entity/custom.png` (custom namespace)

## Details
- **Custom Health**: 40 HP (independent of player health)
- **Following Range**: 2-16 blocks
- **Inventory Size**: 36 slots
- **Support**: Multiple girlfriends per player
- **Compatibility**: Minecraft 1.21.9, Fabric API, Java 21+
- **AI Providers**: Chutes AI (cloud), OpenRouter (cloud), KoboldCpp (local)
- **Skin Support**: Resource pack based with GUI configuration
- **Combat**: Auto-weapon selection, bow combat, owner defense

## License

Based on [Girlfriend Mod](https://github.com/UltimateGamerMC/Girlfriend-Mod) by UltimateGamerMC (CC0 1.0).

Additions by minipasila are licensed under GPLv2.

For full documentation, see `COMPLETION_SUMMARY.md`.