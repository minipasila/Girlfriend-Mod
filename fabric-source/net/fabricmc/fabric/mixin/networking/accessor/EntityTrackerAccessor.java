package net.fabricmc.fabric.mixin.networking.accessor;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

@Mixin(targets = "net/minecraft/server/world/ServerChunkLoadingManager$EntityTracker")
public interface EntityTrackerAccessor {
	@Accessor("listeners")
	Set<PlayerAssociatedNetworkHandler> getPlayersTracking();
}
