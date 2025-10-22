package net.fabricmc.fabric.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.ChunkSection;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin {
	/**
	 * Makes Chunk Sections not have isAir = true modded blocks be replaced with AIR against their will.
	 * Mojang report: https://bugs.mojang.com/browse/MC-232360
	 */
	@Redirect(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"))
	private boolean modifyAirCheck(BlockState blockState) {
		return blockState.isOf(Blocks.AIR) || blockState.isOf(Blocks.CAVE_AIR) || blockState.isOf(Blocks.VOID_AIR);
	}
}
