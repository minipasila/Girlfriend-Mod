# Project Config
```json
{
  "minecraft_version": "1.21.9",
  "name": "Girlfriend Mod",
  "author": "Becky Tidus",
  "mod_git_url": "",
  "enviorment": "server-and-client-side",
  "model": "haiku",
  "objective": "Create a mod that adds an interactive Minecraft girlfriend entity. Add a craftable item that shows up as a diamond and can be used to summon her. also make a command.it should occasionally have endearing phrases it chooses from a very large list, and it should have hearts over its head in hearts when it says it to its summoner (owning player). It should randomly give you gift and also have random phrases when it does that. It should have following behavior (might be useful to take from Wolf pet AI? Idk) with stop/wait commands (with right click maybe). Combat asistance, healing, buff effects. They should be able to sleep with you (same bed lay down). Help you cook food, maybe gather resources. It could also ask what you want to be called and then you can tell it what you want to be called. It should have relationship levels displayed over its head as well as its health. The relationship level should determine if it will sleep with you, how often it says endearing phrases, how loyally it might fight for you, give you gifts..etc And the more it loves you the more it will do for you. Get creative, make it say lots of phrase like when its attacking, gets hurt, when you hurt it. The default skin should be of player 'Preposings' but make it so I can easily add player names to use skins from in a list in the source code. You should be able to feed it to hela it food items. If it dies it dies. It should be persistent over server stop and start. It should allow players to have multiple girlfriends.",
  "debug": false
}
```

# Important Constraints
- ONLY work within this project directory
- DO NOT access files outside of this workspace
- All operations must be contained to the mod project folder

# Code Style
- No comments
- Max compatibility mixins
- Prefer Fabric API when possible over mixins
- If you must use a custom texture, create an empty file for the texture and note the texture name in the README.md file.

# Project Structure
- `fabric-source-code/`: Fabric API source code for Minecraft 1.21.9
- `minecraft-source-code/`: Decompiled Minecraft 1.21.9 source code
- `src/main/java/`: Source code for the mod
- `src/main/resources/`: Resource files for the mod (icon, mixins, fabric.mod.json, etc.)
- `gradle.properties`: Gradle properties for the mod
- `build.gradle`: Gradle build file for the mod

# Mixin Development Guide
Each file in `minecraft-source-code/` has mixin descriptors at the top:
- **External method calls**: Methods this class calls on other Minecraft classes
- **Internal methods**: Private/static methods within the class (common injection points)

These descriptors exclude obvious methods (simple getters/setters, constructors, no-param methods returning primitives, etc.) to reduce noise. Use these to write accurate mixin targets without hallucinating method signatures.

# Completion
When you have completed ALL tasks and the mod is fully implemented and working,
First, create a conscise and basic README.md file in the root of the project directory, simply explaining what the mod does and how to use it.
THEN, you MUST output exactly this message:
```
[FABRICATOR_COMPLETE]
```
This marker will terminate the session automatically.
