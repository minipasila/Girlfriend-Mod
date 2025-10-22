package net.fabricmc.fabric.mixin.biome;

import java.util.Set;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin {
	@Redirect(method = "getBiomes", at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
	private Object getBiomes(Supplier<Set<RegistryEntry<Biome>>> instance) {
		return modifyBiomeSet(instance.get());
	}

	@Unique
	protected Set<RegistryEntry<Biome>> modifyBiomeSet(Set<RegistryEntry<Biome>> biomes) {
		return biomes;
	}
}
