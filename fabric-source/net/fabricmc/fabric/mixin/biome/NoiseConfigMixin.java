package net.fabricmc.fabric.mixin.biome;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.noise.NoiseConfig;

import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;

@Mixin(NoiseConfig.class)
public class NoiseConfigMixin {
	@Shadow
	@Final
	private MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(ChunkGeneratorSettings chunkGeneratorSettings, RegistryEntryLookup<DoublePerlinNoiseSampler.NoiseParameters> arg, long seed, CallbackInfo ci) {
		((MultiNoiseSamplerHooks) (Object) multiNoiseSampler).fabric_setSeed(seed);
	}
}
