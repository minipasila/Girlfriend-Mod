package net.fabricmc.fabric.mixin.tag;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.impl.tag.TagAliasEnabledRegistryWrapper;

/**
 * Adds tag alias support to {@code SimpleRegistry$2}, which is the wrapper used
 * for (TODO: only?) static registries during world creation and world/data reloading.
 */
@Mixin(targets = "net.minecraft.registry.SimpleRegistry$2")
abstract class SimpleRegistry2Mixin<T> implements TagAliasEnabledRegistryWrapper {
	// returns SimpleRegistry.this, which implements TagAliasEnabledRegistry
	@Shadow
	public abstract RegistryWrapper.Impl<T> getBase();

	@Override
	public void fabric_loadTagAliases(Map<TagKey<?>, Set<TagKey<?>>> aliasGroups) {
		((TagAliasEnabledRegistryWrapper) getBase()).fabric_loadTagAliases(aliasGroups);
	}
}
