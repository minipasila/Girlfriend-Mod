package net.fabricmc.fabric.impl.resource.v1;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.resource.featuretoggle.FeatureSet;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

// Used to inject into the ResourceReloader store.
public record SetupMarkerResourceReloader(RegistryWrapper.WrapperLookup registryLookup, FeatureSet featureSet) implements SynchronousResourceReloader {
	@Override
	public void prepareSharedState(Store store) {
		store.put(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY, registryLookup);
		store.put(ResourceLoader.RELOADER_FEATURE_SET_KEY, featureSet);
	}

	@Override
	public void reload(ResourceManager manager) {
		// Do nothing.
	}
}
