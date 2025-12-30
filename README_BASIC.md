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
- **Dynamic Conversations** - Contextual dialogue using Chutes AI
- **Memory System** - Remembers your conversations across sessions
- **Customizable Personality** - Edit system prompt for different behavior
- **Configurable Settings** - Temperature, model, and more via GUI

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

5. **Press G** to configure AI settings

## AI Setup

1. Get a free API key from [chutes.ai](https://chutes.ai)
2. Open AI Config (press `G`)
3. Enter your API key
4. Start chatting with your girlfriend!

## Details

- **Custom Health**: 40 HP (independent of player health)
- **Following Range**: 2-16 blocks
- **Gift Types**: Diamonds, emeralds, apples, golden apples, amethyst shards, poppies
- **Support**: Multiple girlfriends per player
- **Compatibility**: Minecraft 1.21.9, Fabric API, Java 21+

## License

Based on [Girlfriend Mod](https://github.com/UltimateGamerMC/Girlfriend-Mod) by UltimateGamerMC (CC0 1.0).

Additions by minipasila are licensed under GPLv2.

For full documentation, see `COMPLETION_SUMMARY.md`.