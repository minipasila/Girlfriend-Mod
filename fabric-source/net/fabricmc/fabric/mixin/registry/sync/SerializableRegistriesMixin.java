package net.fabricmc.fabric.mixin.registry.sync;

import java.util.Set;
import java.util.function.BiConsumer;

import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.SerializableRegistries;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;

// Implements skipping empty dynamic registries with the SKIP_WHEN_EMPTY sync option.
@Mixin(SerializableRegistries.class)
abstract class SerializableRegistriesMixin {
	/**
	 * Used for tag syncing.
	 */
	@Dynamic("method_45961: Stream.filter in stream")
	@Inject(method = "method_56601", at = @At("HEAD"), cancellable = true)
	private static void filterNonSyncedEntries(DynamicRegistryManager.Entry<?> entry, CallbackInfoReturnable<Boolean> cir) {
		boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(entry.key());

		if (canSkip && entry.value().size() == 0) {
			cir.setReturnValue(false);
		}
	}

	/**
	 * Used for registry serialization.
	 */
	@Dynamic("method_56597: Optional.ifPresent in serialize")
	@Inject(method = "method_56596", at = @At("HEAD"), cancellable = true)
	private static void filterNonSyncedEntriesAgain(Set set, RegistryLoader.Entry entry, DynamicOps dynamicOps, BiConsumer biConsumer, Registry registry, CallbackInfo ci) {
		boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(registry.getKey());

		if (canSkip && registry.size() == 0) {
			ci.cancel();
		}
	}
}
