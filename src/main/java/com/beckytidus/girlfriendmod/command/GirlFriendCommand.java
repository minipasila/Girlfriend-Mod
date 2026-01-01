package com.beckytidus.girlfriendmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;

import java.util.List;

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
                // Dismiss Command Group
                .then(CommandManager.literal("dismiss")
                    // Option 1: Dismiss by Player Owner (Existing)
                    // Usage: /girlfriend dismiss <player>
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            ServerWorld world = context.getSource().getWorld();
                            
                            List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(
                                GirlFriendEntity.class, 
                                player.getBoundingBox().expand(200), 
                                g -> g.getOwner() == player
                            );

                            if (girlfriends.isEmpty()) {
                                context.getSource().sendMessage(Text.literal("No girlfriend found nearby for " + player.getName().getString()));
                                return 0;
                            }

                            int count = 0;
                            for (GirlFriendEntity gf : girlfriends) {
                                gf.discard(); // Permanently remove entity
                                count++;
                            }

                            context.getSource().sendMessage(Text.literal("Dismissed " + count + " girlfriend(s)."));
                            return count;
                        })
                    )
                    // Option 2: Dismiss All (Global Purge)
                    // Usage: /girlfriend dismiss all
                    .then(CommandManager.literal("all")
                        .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                        .executes(context -> {
                            ServerWorld world = context.getSource().getWorld();
                            int count = 0;
                            
                            // Iterate all entities in the server world to find orphaned girlfriends
                            for (Entity entity : world.iterateEntities()) {
                                if (entity instanceof GirlFriendEntity) {
                                    entity.discard();
                                    count++;
                                }
                            }
                            
                            context.getSource().sendMessage(Text.literal("Dismissed " + count + " girlfriend(s) from loaded chunks."));
                            return count;
                        })
                    )
                    // Option 3: Dismiss Nearby (Radius 5)
                    // Usage: /girlfriend dismiss nearby
                    .then(CommandManager.literal("nearby")
                        .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                        .executes(context -> {
                            Entity sourceEntity = context.getSource().getEntity();
                            if (sourceEntity == null) {
                                context.getSource().sendMessage(Text.literal("Command must be run by an entity to use 'nearby'."));
                                return 0;
                            }
                            
                            ServerWorld world = context.getSource().getWorld();
                            // Find any girlfriend within 5 blocks, ignoring ownership
                            List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(
                                GirlFriendEntity.class, 
                                sourceEntity.getBoundingBox().expand(5.0), 
                                g -> true
                            );

                            int count = 0;
                            for (GirlFriendEntity gf : girlfriends) {
                                gf.discard();
                                count++;
                            }

                            if (count == 0) {
                                context.getSource().sendMessage(Text.literal("No girlfriends found within 5 blocks."));
                            } else {
                                context.getSource().sendMessage(Text.literal("Dismissed " + count + " nearby girlfriend(s)."));
                            }
                            return count;
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
                .then(CommandManager.literal("config")
                    .executes(context -> {
                        context.getSource().sendMessage(Text.literal("To configure AI, please use the client-side keybind (Default: G)."));
                        return 1;
                    })
                )
                .then(CommandManager.literal("reloadprompt")
                    .executes(context -> {
                        context.getSource().sendMessage(Text.literal("♥ System prompt will be reloaded on next AI response!"));
                        return 1;
                    })
                )
        );
    }
}