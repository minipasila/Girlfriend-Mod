# Girlfriend Mod

An interactive Minecraft mod that adds a customizable girlfriend entity companion with relationship mechanics, gift-giving, following behavior, and affectionate dialogue.

## Features

- **Girlfriend Entity** - Interactive companion that follows you and sends romantic messages
- **Relationship System** - Level 0-100 affects behavior frequency and loyalty
- **Gift Giving** - Entity periodically gives you diamonds, emeralds, and other items
- **Feeding System** - Right-click to feed food items and gain relationship points
- **Commands**:
  - `/girlfriend summon <player>` - Summon a girlfriend
  - `/girlfriend relationship <player> <0-100>` - Set relationship level
  - `/girlfriend list <player>` - Count girlfriends

## Installation

1. Install Fabric Loader for Minecraft 1.21.9
2. Install Fabric API 0.134.0+1.21.9
3. Place `girlfriend-mod-1.0.0.jar` in your `mods/` folder

## Usage

1. Craft the Summoner Item: 4 Diamonds + 1 Heart of the Sea in a cross pattern
2. Right-click with the item to summon a girlfriend
3. Right-click to feed her food items and increase relationship
4. Sneak + right-click to toggle between follow and wait modes

## Details

- **Custom Health**: 40 HP (independent of player health)
- **Following Range**: 2-16 blocks
- **Phrases**: 30+ romantic messages with ♥ heart symbols
- **Support**: Multiple girlfriends per player
- **Compatibility**: Minecraft 1.21.9, Fabric API, Java 21+

For full feature documentation, see `COMPLETION_SUMMARY.md`.
