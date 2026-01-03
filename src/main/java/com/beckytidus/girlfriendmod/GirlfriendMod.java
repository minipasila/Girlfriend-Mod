package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.event.ChatEventHandler;
import com.beckytidus.girlfriendmod.network.ModNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import com.beckytidus.girlfriendmod.command.GirlFriendCommand;
import com.beckytidus.girlfriendmod.interaction.EntityInteractionHandler;
import com.beckytidus.girlfriendmod.interaction.ItemUseHandler;
import com.beckytidus.girlfriendmod.event.EntityAttributeHandler;

import java.util.List;

public class GirlfriendMod implements ModInitializer {
    public static final String MOD_ID = "girlfriend-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Girlfriend Mod with AI...");

        ModConfig.load();
        ModNetwork.register();
        ChatEventHandler.register();

        EntityRegistry.register();
        ItemRegistry.register();
        EntityAttributeHandler.register();
        GirlFriendCommand.register();
        EntityInteractionHandler.register();
        ItemUseHandler.register();

        registerPlayerLifecycleEvents();
        registerPlayerKillEvents(); // <-- NEW: Add player kill event listener

        LOGGER.info("Girlfriend Mod loaded successfully!");
    }

    private void registerPlayerLifecycleEvents() {
        // Event: Player Dies
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof PlayerEntity player && !player.getEntityWorld().isClient()) {
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(
                    GirlFriendEntity.class,
                    player.getBoundingBox().expand(100),
                    g -> g.getOwner() == player
                );

                for (GirlFriendEntity gf : girlfriends) {
                    gf.onOwnerDied(player);
                }
            }
        });

        // Event: Player Respawns
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            ServerWorld world = (ServerWorld) newPlayer.getEntityWorld();

            // Find girlfriends belonging to this player (using UUID which stays constant)
            // We search a large area because the player might have respawned far away from where they died
            // Note: If chunks are unloaded, this might miss them until they are loaded.
            // For a robust system, we would need a global tracking system, but for now,
            // we assume the GF is in loaded chunks (e.g., near death point) or will catch up when chunks load.

            // Actually, we should look in the world where they might be.
            // If the player respawned in a different dimension, the GF won't be found here immediately.
            // This implementation assumes same-dimension respawn or GF is in the current world.

            List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(
                GirlFriendEntity.class,
                newPlayer.getBoundingBox().expand(200), // Search reasonably wide
                g -> g.getOwner() != null && g.getOwner().getUuid().equals(newPlayer.getUuid())
            );

            for (GirlFriendEntity gf : girlfriends) {
                gf.onOwnerRespawned(newPlayer);
            }
        });
    }

    // NEW: Register player kill events
    private void registerPlayerKillEvents() {
        // Event: Player Kills Entity
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            // Only react to hostile mob deaths
            if (entity instanceof Monster || entity instanceof HostileEntity) {
                // Check if a player killed this entity
                Entity attacker = source.getAttacker();
                if (attacker instanceof PlayerEntity player && !player.getEntityWorld().isClient()) {
                    ServerWorld world = (ServerWorld) player.getEntityWorld();

                    // Find nearby girlfriends belonging to this player
                    List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(
                        GirlFriendEntity.class,
                        player.getBoundingBox().expand(20), // 20 block radius
                        g -> g.getOwner() == player
                    );

                    for (GirlFriendEntity gf : girlfriends) {
                        gf.onOwnerKilledMob(entity);
                    }
                }
            }
        });
    }
}
