# Girlfriend Mod

An interactive Minecraft mod that adds an affectionate girlfriend entity companion to Minecraft 1.21.9 with Fabric Loader. Create a customizable companion with relationship mechanics, gift-giving, combat assistance, and various romantic interactions.

## Implemented Features

### Core Entity System
- **Interactive Girlfriend Entity** - Custom entity that follows players, sends messages, and displays relationship status
- **Relationship System** - Track relationship level (0-100) that affects behavior, phrase frequency, and combat loyalty
- **Health System** - Custom health tracking (40 HP max) independent of player health bar
- **Auto-Healing** - Entity automatically heals (+5 HP) when below 70% health every 40 seconds
- **Persistence System** - Girlfriend data (relationship, health, custom name) saves/loads across server restarts

### Interaction Features
- **Following Behavior** - Entity intelligently follows player, maintaining distance, stops when within 2 blocks
- **Feeding System** - Right-click interaction to feed various food items:
  - Apples/Golden Apples: +5 HP, +5 relationship
  - Wheat/Bread: +2 HP, +2 relationship
  - Carrots/Potatoes/Baked Potatoes: +3 HP, +3 relationship
  - Pumpkin Pie/Cake: +6 HP, +4 relationship
- **Sneak-Click Toggle** - Sneaking + right-click toggles between follow and wait modes
- **Custom Naming** - Set personalized names for your girlfriend

### Affection & Dialogue
- **30+ Endearing Phrases** - Large pool of romantic dialogue messages with ♥ heart symbols:
  - "I love you so much!", "You're my everything", "Together we're unstoppable!"
  - "You make my heart skip a beat", "I'd do anything for you", and many more
- **Relationship-Based Frequency** - Higher relationship increases phrase frequency (15-25 second intervals)
- **Damage Reactions** - Unique messages when girlfriend takes damage
- **Encouragement System** - Special responses when fed delicious food

### Gift & Reward System
- **Random Gift Giving** - Entity periodically gives gifts to player (60-90 second intervals)
- **Gift Items** - Diamonds, Emeralds, Apples, Golden Apples, Amethyst Shards, Flowers
- **Relationship Scaling** - Higher relationship increases gift frequency up to 33% more often
- **Smart Inventory** - Gifts added to inventory, dropped if full

### Commands & Summoning
- **Craftable Item** - Recipe: Diamond + Heart of the Sea + Diamond (center cross pattern)
- **Right-Click Summon** - Use item to spawn girlfriend at player location
- **Command Summoning** - `/girlfriend summon <player>` - Creates girlfriend for specified player
- **Relationship Control** - `/girlfriend relationship <player> <0-100>` - Modify girlfriend relationship
- **List Command** - `/girlfriend list <player>` - Count girlfriends owned by player

### Item Recipes
**GirlFriend Summoner Item Recipe:**
```
 D
DHD
 D
```
Where D = Diamond, H = Heart of the Sea

## Technical Details

### Project Structure
- `src/main/java/`: Core mod implementation
  - `entity/`: GirlFriendEntity and related classes
  - `registry/`: Entity and item registration
  - `item/`: GirlFriendSummonerItem
  - `command/`: GirlFriend command implementation
  - `interaction/`: Entity interaction handlers

- `src/main/resources/`: Resource files
  - `data/girlfriend-mod/recipes/`: Recipe definitions
  - `assets/girlfriend-mod/models/`: Item models
  - `assets/girlfriend-mod/textures/`: Texture placeholders

### Dependencies
- Minecraft 1.21.9
- Fabric API
- Fabric Loader 0.17.3+
- Java 21+

## Usage

1. **Craft a Summoner Item**: Combine 4 diamonds + 1 heart of the sea in the specified pattern
2. **Summon Your GirlFriend**: Right-click with the summoner item or use `/girlfriend summon @s`
3. **Feed & Bond**: Feed her food items to increase relationship
4. **Give Commands**: Right-click to interact
5. **Custom Name**: Set a personalized name using the interaction system

### Combat Features
- **Protective AI** - Entity attacks hostile mobs and players who harm owner
- **Damage Response** - Girlfriend responds to attacks with combat phrases
- **Combat Support** - Fights alongside player during battles
- **Revenge Mechanic** - Retaliates against attackers with -3 relationship penalty

### Display & Customization
- **Name Tags** - Shows custom name, relationship level, and health above entity head
- **Health Display** - Format: "Name [Lv:45] [HP:30/40]"
- **Heart Particles** - Visual effects when girlfriend expresses affection
- **Custom Names** - Use Name Tag or commands to rename your girlfriend

### Language Support
- **English Localization** - Full en_us.json language file for UI elements

## Relationship Mechanics

- **Level 0-25**: Limited interaction, basic phrases every 25 seconds
- **Level 26-50**: More frequent gifts (every ~75 seconds) and affectionate messages (every ~22 seconds)
- **Level 51-75**: Enhanced combat support, frequent gifts and dialogue
- **Level 76-100**: Maximum loyalty, frequent gifts (every ~62 seconds), intense affection with dialogue every ~15 seconds
- **At Level 50+**: Can sleep with you in beds

## Technical Specifications

- **Entity Size**: 0.9 width × 1.9 height (human-like proportions)
- **Health**: 40 HP max (custom, independent of Minecraft health)
- **Movement Speed**: 0.2-0.3 blocks/second (when following)
- **Follow Range**: Up to 16 blocks away before closing distance
- **Relationship Ticks**: Updated every game tick (20 ticks per second)

## Building from Source

```bash
./gradlew build
```

Output JAR: `build/libs/girlfriend-mod-1.0.0.jar`

## Installation

1. Install Fabric Loader for Minecraft 1.21.9
2. Install Fabric API mod version 0.134.0+1.21.9 or later
3. Place the compiled girlfriend-mod JAR in your mods folder
4. Launch Minecraft with Fabric profile

## Known Limitations & In-Development Features

Due to Minecraft 1.21.9's significant API changes, the following features are incomplete:

- **Custom Textures** - Needs humanoid model implementation
- **Custom Skins** - Planned support for loading player skins from configurable names list
- **Sleep Mechanics** - Check exists but needs full bed interaction
- **Resource Gathering** - AI framework ready but needs implementation
- **Cooking Assistance** - Planned feature for multi-block crafting help
- **Sound Effects** - Planned voice lines and ambient sounds
- **NBT Persistence** - Core logic done, needs WriteView API adaptation

## Customization Guide

### Adding More Phrases

Edit `GirlFriendEntity.java`, method `sayRandomPhrase()`:

```java
String[] phrases = {
    "I love you so much!",
    "Your phrase here!",
    // Add more phrases to the array
};
```

### Modifying Gift Items

Edit method `getRandomGiftItem()` in `GirlFriendEntity.java`:

```java
ItemStack[] possibleGifts = {
    new ItemStack(Items.DIAMOND),
    new ItemStack(Items.YOUR_ITEM_HERE),
    // Add more items
};
```

### Adjusting Relationship Scaling

In the `tick()` method, modify these values:
- `phraseFrequency = Math.max(15000, 25000 - (relationshipLevel * 100));`
- `giftFrequency = Math.max(60000, 90000 - (relationshipLevel * 200));`

## Credits

Created by Becky Tidus for Minecraft 1.21.9

## License

MIT License - Feel free to modify and redistribute

---

**Status**: Beta - Core features implemented and functional. Some Minecraft 1.21.9 API adaptations pending for full compilation and deployment.
