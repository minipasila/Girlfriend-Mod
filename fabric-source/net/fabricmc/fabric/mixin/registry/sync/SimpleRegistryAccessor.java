package net.fabricmc.fabric.mixin.registry.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.registry.SimpleRegistry;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor {
	@Accessor
	boolean isFrozen();
}
