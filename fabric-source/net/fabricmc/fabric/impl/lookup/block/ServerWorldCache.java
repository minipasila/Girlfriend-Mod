package net.fabricmc.fabric.impl.lookup.block;

import net.minecraft.util.math.BlockPos;

/**
 * Allows attachment of a BlockApiCache to a {@link net.minecraft.server.world.ServerWorld}.
 */
public interface ServerWorldCache {
	void fabric_registerCache(BlockPos pos, BlockApiCacheImpl<?, ?> cache);

	void fabric_invalidateCache(BlockPos pos);
}
