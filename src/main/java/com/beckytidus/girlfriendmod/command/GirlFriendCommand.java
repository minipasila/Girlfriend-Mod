package com.beckytidus.girlfriendmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;

public class GirlFriendCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerGirlFriendCommand(dispatcher);
        });
    }

    private static void registerGirlFriendCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("girlfriend")
                .then(CommandManager.literal("summon")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            ServerWorld world = context.getSource().getWorld();
                            GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, world);
                            girlfriend.setPosition(player.getX(), player.getY(), player.getZ());
                            girlfriend.setOwner(player);
                            world.spawnEntity(girlfriend);
                            context.getSource().sendMessage(Text.literal("♥ GirlFriend summoned for " + player.getName().getString()));
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("relationship")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 100))
                            .executes(context -> {
                                PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                int level = IntegerArgumentType.getInteger(context, "level");
                                ServerWorld world = context.getSource().getWorld();

                                for (GirlFriendEntity girlfriend : world.getEntitiesByClass(GirlFriendEntity.class, player.getBoundingBox().expand(100), g -> g.getOwner() == player)) {
                                    girlfriend.setRelationshipLevel(level);
                                    context.getSource().sendMessage(Text.literal("♥ Set relationship to " + level));
                                    return 1;
                                }
                                context.getSource().sendMessage(Text.literal("No girlfriend found for this player"));
                                return 0;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("list")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            ServerWorld world = context.getSource().getWorld();
                            var girlfriends = world.getEntitiesByClass(GirlFriendEntity.class, player.getBoundingBox().expand(100), g -> g.getOwner() == player);
                            context.getSource().sendMessage(Text.literal("Girlfriends for " + player.getName().getString() + ": " + girlfriends.size()));
                            return 1;
                        })
                    )
                )
                // Add to registerGirlFriendCommand...
                .then(CommandManager.literal("config")
                    .executes(context -> {
                        // Since config is client side GUI, we can't open it directly from server command.
                        // But for single player integration or if installed on client,
                        // we usually use a Keybinding or ModMenu.
                        // For this specific requirement, we'll send a message telling user to use Keybind or ModMenu
                        // OR if this is Client Side logic running, we open it.
                        // Best approach for command-based GUI opening in Fabric:
                        context.getSource().sendMessage(Text.literal("To configure AI, please install ModMenu or use the client-side keybind (if configured)."));
                        return 1;
                    })
                )
                // Add this inside registerGirlFriendCommand() after the "config" literal:
                .then(CommandManager.literal("reloadprompt")
                    .executes(context -> {
                        // Clear cached prompt if you're caching it
                        // The next AI call will reload from file automatically
                        context.getSource().sendMessage(Text.literal("♥ System prompt will be reloaded on next AI response!"));
                        return 1;
                    })
                )
        );
    }
}
