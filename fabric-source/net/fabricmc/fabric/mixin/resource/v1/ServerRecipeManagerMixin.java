package net.fabricmc.fabric.mixin.resource.v1;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryWrapper;

import net.fabricmc.fabric.impl.resource.v1.FabricRecipeManager;

@Mixin(ServerRecipeManager.class)
public class ServerRecipeManagerMixin implements FabricRecipeManager {
	@Unique
	private RegistryWrapper.WrapperLookup registries;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
		this.registries = registries;
	}

	@Override
	public RegistryWrapper.WrapperLookup fabric$getRegistries() {
		return Objects.requireNonNull(registries);
	}
}
