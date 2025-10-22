package net.fabricmc.fabric.mixin.loot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.resource.ResourceFinder;

@Mixin(ResourceFinder.class)
public interface ResourceFinderAccessor {
	@Accessor
	String getDirectoryName();
}
