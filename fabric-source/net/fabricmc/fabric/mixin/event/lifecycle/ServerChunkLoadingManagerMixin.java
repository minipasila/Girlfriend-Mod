package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ServerChunkLoadingManagerMixin {
	@Shadow
	@Final
	ServerWorld world;

	/**
	 * Injection is inside of tryUnloadChunk.
	 * We inject just after "setLoadedToWorld" is made false, since here the WorldChunk is guaranteed to be unloaded.
	 */
	@Inject(method = "method_60440", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;save(Lnet/minecraft/world/chunk/Chunk;)Z"))
	private void onChunkUnload(ChunkHolder chunkHolder, CompletableFuture<?> completableFuture, long l, CallbackInfo ci, @Local Chunk chunk) {
		if (chunk instanceof WorldChunk worldChunk) {
			ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, worldChunk);
		}
	}
}
