package net.fabricmc.fabric.mixin.registry.sync;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

// Vanilla doesn't mark namespaces in the directories of tags and dynamic registry elements at all,
// so we prepend the directories with the namespace if it's a modded registry id.
@Mixin(RegistryKeys.class)
public class RegistryKeysMixin {
	@ModifyReturnValue(method = "getPath", at = @At("RETURN"))
	private static String prependDirectoryWithNamespace(String original, @Local(argsOnly = true) RegistryKey<? extends Registry<?>> registryRef) {
		Identifier id = registryRef.getValue();

		if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			return id.getNamespace() + "/" + id.getPath();
		}

		return original;
	}

	@ModifyReturnValue(method = "getTagPath", at = @At("RETURN"))
	private static String prependTagDirectoryWithNamespace(String original, @Local(argsOnly = true) RegistryKey<? extends Registry<?>> registryRef) {
		Identifier id = registryRef.getValue();

		if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			return "tags/" + id.getNamespace() + "/" + id.getPath();
		}

		return original;
	}
}
