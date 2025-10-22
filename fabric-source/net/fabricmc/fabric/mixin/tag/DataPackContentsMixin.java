package net.fabricmc.fabric.mixin.tag;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;

import net.fabricmc.fabric.impl.tag.TagAliasLoader;

/**
 * Applies pending tag aliases to dynamic (including reloadable) registries.
 * The priority is 999 because it must apply the injection to applyPendingTagLoads before the tag loaded lifecycle event.
 */
@Mixin(value = DataPackContents.class, priority = 999)
abstract class DataPackContentsMixin {
	@Unique
	private CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistriesByType;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void storeDynamicRegistries(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, List<?> pendingTagLoads, int functionPermissionLevel, CallbackInfo info) {
		dynamicRegistriesByType = dynamicRegistries;
	}

	@Inject(method = "applyPendingTagLoads", at = @At("RETURN"))
	private void applyDynamicTagAliases(CallbackInfo info) {
		// Note: when using /reload, dynamic registry tag reloading goes through the same system that is also used
		// for static registries. Luckily, it doesn't break anything to run the code below even in that case,
		// since the map of pending tag alias groups is cleared after they're applied in the first round.
		// This code also needs to run after the vanilla code so the pending tag reloads don't override
		// the alias groups for dynamic registries.
		TagAliasLoader.applyToDynamicRegistries(dynamicRegistriesByType, ServerDynamicRegistryType.WORLDGEN);
		TagAliasLoader.applyToDynamicRegistries(dynamicRegistriesByType, ServerDynamicRegistryType.RELOADABLE);
	}
}
