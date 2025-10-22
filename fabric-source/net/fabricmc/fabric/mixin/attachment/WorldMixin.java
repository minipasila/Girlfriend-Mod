package net.fabricmc.fabric.mixin.attachment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;

import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;

@Mixin(World.class)
abstract class WorldMixin implements AttachmentTargetImpl {
	@Shadow
	public abstract boolean isClient();

	@Shadow
	public abstract DynamicRegistryManager getRegistryManager();

	@Override
	public boolean fabric_shouldTryToSync() {
		return !this.isClient();
	}

	@Override
	public DynamicRegistryManager fabric_getDynamicRegistryManager() {
		return getRegistryManager();
	}
}
