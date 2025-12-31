# Girlfriend Mod

An interactive Minecraft mod that adds a customizable girlfriend entity companion with relationship mechanics, gift-giving, following behavior, and AI-powered conversations.

## Features

### Core Features
- **Girlfriend Entity** - Interactive companion that follows you
- **Relationship System** - Level 0-100 affects behavior frequency
- **Gift Giving** - Entity periodically gives diamonds, emeralds, and other items
- **Feeding System** - Right-click to feed food items and increase relationship
- **Following Behavior** - Toggle follow/wait with sneak+right-click

### AI Features (New)
- **Dynamic Conversations** - Contextual dialogue using Chutes AI or OpenRouter
- **Multiple AI Providers** - Choose between Chutes AI (default) or OpenRouter
- **Memory System** - Remembers your conversations across sessions
- **Customizable Personality** - Edit system prompt for different behavior
- **Configurable Settings** - Temperature, model, and more via GUI

### Custom Skins (New)
- **Resource Pack Support** - Create custom skins using standard Minecraft resource packs
- **GUI Configuration** - Set texture path directly in the AI Config screen
- **Flexible Format** - Supports any texture path format (namespace:path)

### Commands
- `/girlfriend summon <player>` - Summon a girlfriend
- `/girlfriend relationship <player> <0-100>` - Set relationship level
- `/girlfriend list <player>` - Count girlfriends
- `/girlfriend reloadprompt` - Reload system prompt

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
3. **Right-click** to feed her food items and increase relationship
4. **Sneak + Right-click** to toggle between follow and wait modes
5. **Press G** to configure AI settings (including provider selection and skin)

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

## AI Setup

### Option 1: Chutes AI (Default)
1. Get a free API key from [chutes.ai](https://chutes.ai)
2. Open AI Config (press `G`)
3. Select "Chutes AI" as provider
4. Enter your API key
5. Start chatting with your girlfriend!

### Option 2: OpenRouter
1. Get an API key from [openrouter.ai](https://openrouter.ai)
2. Open AI Config (press `G`)
3. Select "OpenRouter" as provider
4. Enter your API key
5. Choose your preferred model (e.g., claude-sonnet-4-20250514)
6. Start chatting with your girlfriend!

## Details
- **Custom Health**: 40 HP (independent of player health)
- **Following Range**: 2-16 blocks
- **Gift Types**: Diamonds, emeralds, apples, golden apples, amethyst shards, poppies
- **Support**: Multiple girlfriends per player
- **Compatibility**: Minecraft 1.21.9, Fabric API, Java 21+
- **AI Providers**: Chutes AI (default), OpenRouter (alternative)
- **Skin Support**: Resource pack based with GUI configuration

## License

Based on [Girlfriend Mod](https://github.com/UltimateGamerMC/Girlfriend-Mod) by UltimateGamerMC (CC0 1.0).

Additions by minipasila are licensed under GPLv2.

For full documentation, see `COMPLETION_SUMMARY.md`.