package net.fabricmc.fabric.mixin.object.builder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

import net.fabricmc.fabric.impl.object.builder.FabricTrackedDataRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;

@Mixin(TrackedDataHandlerRegistry.class)
abstract class TrackedDataHandlerRegistryMixin {
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void storeVanillaHandlers(CallbackInfo ci) {
		FabricTrackedDataRegistryImpl.storeVanillaHandlers();
	}

	@Inject(method = "register(Lnet/minecraft/entity/data/TrackedDataHandler;)V", at = @At("HEAD"))
	private static void onHeadRegister(TrackedDataHandler<?> handler, CallbackInfo ci) {
		if (FabricTrackedDataRegistryImpl.hasStoredVanillaHandlers() && FabricLoader.getInstance().isDevelopmentEnvironment()) {
			throw new IllegalStateException("Tried to register tracked data handler " + handler + " using TrackedDataHandlerRegistry.register. This is not allowed as it can lead to desynchronization issues; use FabricTrackedDataRegistry.register instead.");
		}
	}
}
