package net.fabricmc.fabric.mixin.registry.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.Registries;

@Mixin(Registries.class)
public class RegistriesMixin {
	@Unique
	private static boolean hasInitialised = false;

	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private static void init(CallbackInfo ci) {
		if (hasInitialised) {
			ci.cancel();
		}

		hasInitialised = true;
	}
}
