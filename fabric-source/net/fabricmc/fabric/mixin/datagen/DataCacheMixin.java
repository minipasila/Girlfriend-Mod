package net.fabricmc.fabric.mixin.datagen;

import java.time.LocalDateTime;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.data.DataCache;

@Mixin(DataCache.class)
public abstract class DataCacheMixin {
	// Lambda in write()V
	@Redirect(method = "method_46571", at = @At(value = "INVOKE", target = "Ljava/time/LocalDateTime;now()Ljava/time/LocalDateTime;"))
	private LocalDateTime constantTime() {
		// Write a constant time to the .cache file to ensure datagen output is reproducible
		return LocalDateTime.MIN;
	}
}
