package net.fabricmc.fabric.mixin.registry.sync;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.registry.RegistryLoader;
import net.minecraft.server.SaveLoading;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

// Implements dynamic registry loading.
@Mixin(SaveLoading.class)
abstract class SaveLoadingMixin {
	@ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryLoader;loadFromResource(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Immutable;", ordinal = 0), index = 2, allow = 1)
	private static List<RegistryLoader.Entry<?>> modifyLoadedEntries(List<RegistryLoader.Entry<?>> entries) {
		return DynamicRegistries.getDynamicRegistries();
	}
}
