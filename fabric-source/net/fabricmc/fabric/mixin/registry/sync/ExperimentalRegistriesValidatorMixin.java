package net.fabricmc.fabric.mixin.registry.sync;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.registry.ExperimentalRegistriesValidator;
import net.minecraft.registry.RegistryLoader;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

@Mixin(ExperimentalRegistriesValidator.class)
class ExperimentalRegistriesValidatorMixin {
	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/registry/RegistryLoader;DYNAMIC_REGISTRIES:Ljava/util/List;"), method = "method_54839")
	private static List<RegistryLoader.Entry<?>> getDynamicRegistries() {
		// Register cloners for all dynamic registries.
		return DynamicRegistries.getDynamicRegistries();
	}
}
