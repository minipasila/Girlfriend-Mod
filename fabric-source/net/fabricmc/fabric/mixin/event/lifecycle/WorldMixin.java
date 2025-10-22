package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;

@Mixin(World.class)
public abstract class WorldMixin implements LoadedChunksCache {
	@Shadow
	public abstract boolean isClient();

	@Unique
	private final Set<WorldChunk> loadedChunks = new HashSet<>();

	@Override
	public Set<WorldChunk> fabric_getLoadedChunks() {
		return this.loadedChunks;
	}

	@Override
	public void fabric_markLoaded(WorldChunk chunk) {
		this.loadedChunks.add(chunk);
	}

	@Override
	public void fabric_markUnloaded(WorldChunk chunk) {
		this.loadedChunks.remove(chunk);
	}
}
