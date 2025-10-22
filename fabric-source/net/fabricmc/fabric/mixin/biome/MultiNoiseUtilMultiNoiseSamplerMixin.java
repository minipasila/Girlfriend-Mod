package net.fabricmc.fabric.mixin.biome;

import com.google.common.base.Preconditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;

@Mixin(MultiNoiseUtil.MultiNoiseSampler.class)
public class MultiNoiseUtilMultiNoiseSamplerMixin implements MultiNoiseSamplerHooks {
	@Unique
	private Long seed = null;

	@Unique
	private PerlinNoiseSampler endBiomesSampler = null;

	@Override
	public void fabric_setSeed(long seed) {
		this.seed = seed;
	}

	@Override
	public long fabric_getSeed() {
		return this.seed;
	}

	@Override
	public PerlinNoiseSampler fabric_getEndBiomesSampler() {
		if (endBiomesSampler == null) {
			Preconditions.checkState(seed != null, "MultiNoiseSampler doesn't have a seed set, created using different method?");
			endBiomesSampler = new PerlinNoiseSampler(new ChunkRandom(new CheckedRandom(seed)));
		}

		return endBiomesSampler;
	}
}
