package net.fabricmc.fabric.impl.biome.modification;

import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.Biome;

/**
 * Utility class for accessing the worldgen data that vanilla uses to generate its vanilla datapack.
 */
public final class BuiltInRegistryKeys {
	private static final RegistryWrapper.WrapperLookup vanillaRegistries = BuiltinRegistries.createWrapperLookup();

	private BuiltInRegistryKeys() {
	}

	public static boolean isBuiltinBiome(RegistryKey<Biome> key) {
		return biomeRegistryWrapper().getOptional(key).isPresent();
	}

	public static RegistryEntryLookup<Biome> biomeRegistryWrapper() {
		return vanillaRegistries.getOrThrow(RegistryKeys.BIOME);
	}
}
