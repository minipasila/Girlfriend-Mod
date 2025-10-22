package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
	@Unique
	private DynamicRegistryManager dynamicRegistryManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, RegistryWrapper.WrapperLookup wrapperLookup, FeatureSet featureSet, CommandManager.RegistrationEnvironment registrationEnvironment, List list, int i, CallbackInfo ci) {
		dynamicRegistryManager = combinedDynamicRegistries.getCombinedRegistryManager();
	}

	@Inject(method = "applyPendingTagLoads", at = @At("TAIL"))
	private void hookRefresh(CallbackInfo ci) {
		CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(dynamicRegistryManager, false);
	}
}
