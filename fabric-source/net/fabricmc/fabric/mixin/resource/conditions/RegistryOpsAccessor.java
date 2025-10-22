package net.fabricmc.fabric.mixin.resource.conditions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.registry.RegistryOps;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccessor {
	@Accessor
	RegistryOps.RegistryInfoGetter getRegistryInfoGetter();
}
