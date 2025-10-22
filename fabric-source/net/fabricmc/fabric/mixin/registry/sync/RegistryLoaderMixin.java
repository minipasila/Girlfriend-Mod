package net.fabricmc.fabric.mixin.registry.sync;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryWrapper;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistryViewImpl;

@Mixin(RegistryLoader.class)
public class RegistryLoaderMixin {
	@Unique
	private static final ThreadLocal<Boolean> IS_SERVER = ThreadLocal.withInitial(() -> false);

	/**
	 * Sets IS_SERVER flag. Note that this must be reset after call, as the render thread
	 * invokes this method as well.
	 */
	@WrapOperation(method = "loadFromResource(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Immutable;", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryLoader;load(Lnet/minecraft/registry/RegistryLoader$RegistryLoadable;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Immutable;"))
	private static DynamicRegistryManager.Immutable wrapIsServerCall(@Coerce Object registryLoadable, List<RegistryWrapper.Impl<?>> baseRegistries, List<RegistryLoader.Entry<?>> entries, Operation<DynamicRegistryManager.Immutable> original) {
		try {
			IS_SERVER.set(true);
			return original.call(registryLoadable, baseRegistries, entries);
		} finally {
			IS_SERVER.set(false);
		}
	}

	@Inject(
			method = "load(Lnet/minecraft/registry/RegistryLoader$RegistryLoadable;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/registry/DynamicRegistryManager$Immutable;",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
					ordinal = 0
			)
	)
	private static void beforeLoad(@Coerce Object registryLoadable, List<RegistryWrapper.Impl<?>> baseRegistries, List<RegistryLoader.Entry<?>> entries, CallbackInfoReturnable<DynamicRegistryManager.Immutable> cir, @Local(ordinal = 2) List<RegistryLoader.Loader<?>> registriesList) {
		if (!IS_SERVER.get()) return;

		Map<RegistryKey<? extends Registry<?>>, Registry<?>> registries = new IdentityHashMap<>(registriesList.size());

		for (RegistryLoader.Loader<?> entry : registriesList) {
			registries.put(entry.registry().getKey(), entry.registry());
		}

		DynamicRegistrySetupCallback.EVENT.invoker().onRegistrySetup(new DynamicRegistryViewImpl(registries));
	}
}
