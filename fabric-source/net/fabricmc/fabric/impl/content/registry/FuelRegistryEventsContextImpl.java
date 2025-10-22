package net.fabricmc.fabric.impl.content.registry;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

import net.fabricmc.fabric.api.registry.FuelRegistryEvents;

public record FuelRegistryEventsContextImpl(RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures, int baseSmeltTime) implements FuelRegistryEvents.Context {
}
