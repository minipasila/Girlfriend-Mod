package net.fabricmc.fabric.mixin.networking.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.world.ServerChunkLoadingManager;

@Mixin(ServerChunkLoadingManager.class)
public interface ServerChunkLoadingManagerAccessor {
	@Accessor
	Int2ObjectMap<EntityTrackerAccessor> getEntityTrackers();
}
