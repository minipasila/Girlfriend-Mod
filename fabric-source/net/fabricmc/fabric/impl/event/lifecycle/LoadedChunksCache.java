package net.fabricmc.fabric.impl.event.lifecycle;

import java.util.Set;

import net.minecraft.world.chunk.WorldChunk;

/**
 * A simple marker interface which holds references to chunks which block entities may be loaded or unloaded from.
 */
public interface LoadedChunksCache {
	Set<WorldChunk> fabric_getLoadedChunks();

	/**
	 * Marks a chunk as loaded in a world.
	 */
	void fabric_markLoaded(WorldChunk chunk);

	/**
	 * Marks a chunk as unloaded in a world.
	 */
	void fabric_markUnloaded(WorldChunk chunk);
}
