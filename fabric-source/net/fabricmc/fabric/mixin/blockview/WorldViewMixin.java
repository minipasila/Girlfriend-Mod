package net.fabricmc.fabric.mixin.blockview;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

@Mixin(WorldView.class)
public interface WorldViewMixin extends BlockRenderView {
	@Shadow
	RegistryEntry<Biome> getBiome(BlockPos pos);

	@Override
	default boolean hasBiomes() {
		return true;
	}

	@Override
	default RegistryEntry<Biome> getBiomeFabric(BlockPos pos) {
		return getBiome(pos);
	}
}
