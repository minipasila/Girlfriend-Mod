package net.fabricmc.fabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.Registries;

import net.fabricmc.fabric.impl.item.DefaultItemComponentImpl;

@Mixin(Registries.class)
public abstract class RegistriesMixin {
	@Inject(method = "freezeRegistries", at = @At("HEAD"))
	private static void modifyDefaultItemComponents(CallbackInfo ci) {
		DefaultItemComponentImpl.modifyItemComponents();
	}
}
