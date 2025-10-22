package net.fabricmc.fabric.mixin.biome;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;

import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;

@Mixin(ChunkNoiseSampler.class)
public class ChunkNoiseSamplerMixin {
	@Unique
	private long seed;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(int horizontalSize, NoiseConfig noiseConfig, int i, int j, GenerationShapeConfig generationShapeConfig, DensityFunctionTypes.Beardifying arg, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender, CallbackInfo ci) {
		seed = ((MultiNoiseSamplerHooks) (Object) noiseConfig.getMultiNoiseSampler()).fabric_getSeed();
	}

	@Inject(method = "createMultiNoiseSampler", at = @At("RETURN"))
	private void createMultiNoiseSampler(NoiseRouter noiseRouter, List<MultiNoiseUtil.NoiseHypercube> list, CallbackInfoReturnable<MultiNoiseUtil.MultiNoiseSampler> cir) {
		((MultiNoiseSamplerHooks) (Object) cir.getReturnValue()).fabric_setSeed(seed);
	}
}
