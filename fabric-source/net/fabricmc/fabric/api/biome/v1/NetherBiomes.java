package net.fabricmc.fabric.api.biome.v1;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import net.fabricmc.fabric.impl.biome.NetherBiomeData;

/**
 * API that exposes the internals of Minecraft's nether biome code.
 */
public final class NetherBiomes {
	private NetherBiomes() {
	}

	/**
	 * Adds a biome to the Nether generator.
	 *
	 * @param biome           The biome to add. Must not be null.
	 * @param mixedNoisePoint data about the given {@link Biome}'s spawning information in the nether.
	 * @see MultiNoiseUtil.NoiseValuePoint
	 */
	public static void addNetherBiome(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseValuePoint mixedNoisePoint) {
		NetherBiomeData.addNetherBiome(biome, MultiNoiseUtil.createNoiseHypercube(
				mixedNoisePoint.temperatureNoise(),
				mixedNoisePoint.humidityNoise(),
				mixedNoisePoint.continentalnessNoise(),
				mixedNoisePoint.erosionNoise(),
				mixedNoisePoint.depth(),
				mixedNoisePoint.weirdnessNoise(),
				0
		));
	}

	/**
	 * Adds a biome to the Nether generator.
	 *
	 * @param biome           The biome to add. Must not be null.
	 * @param mixedNoisePoint data about the given {@link Biome}'s spawning information in the nether.
	 * @see MultiNoiseUtil.NoiseHypercube
	 */
	public static void addNetherBiome(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube mixedNoisePoint) {
		NetherBiomeData.addNetherBiome(biome, mixedNoisePoint);
	}

	/**
	 * Returns true if the given biome can generate in the nether, considering the Vanilla nether biomes,
	 * and any biomes added to the Nether by mods.
	 */
	public static boolean canGenerateInNether(RegistryKey<Biome> biome) {
		return NetherBiomeData.canGenerateInNether(biome);
	}
}
