package net.fabricmc.fabric.mixin.item;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.resource.Resource;

import net.fabricmc.fabric.impl.item.EnchantmentUtil;

@Mixin(RegistryLoader.class)
abstract class RegistryLoaderMixin {
	@WrapOperation(
			method = "parseAndAdd",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/registry/MutableRegistry;add(Lnet/minecraft/registry/RegistryKey;Ljava/lang/Object;Lnet/minecraft/registry/entry/RegistryEntryInfo;)Lnet/minecraft/registry/entry/RegistryEntry$Reference;"
			)
	)
	@SuppressWarnings("unchecked")
	private static <T> RegistryEntry.Reference<T> enchantmentKey(
			MutableRegistry<T> instance,
			RegistryKey<T> objectKey,
			Object object,
			RegistryEntryInfo registryEntryInfo,
			Operation<RegistryEntry.Reference<T>> original,
			MutableRegistry<T> registry,
			Decoder<T> decoder,
			RegistryOps<JsonElement> ops,
			RegistryKey<T> registryKey,
			Resource resource,
			RegistryEntryInfo entryInfo
	) {
		if (object instanceof Enchantment enchantment) {
			Enchantment modified = EnchantmentUtil.modify((RegistryKey<Enchantment>) objectKey, enchantment, EnchantmentUtil.determineSource(resource));

			if (modified != null) {
				object = modified;

				// Clear the knownPackInfo to force the server to sync the data pack to the client
				registryEntryInfo = new RegistryEntryInfo(Optional.empty(), registryEntryInfo.lifecycle());
			}
		}

		return original.call(instance, registryKey, object, registryEntryInfo);
	}
}
