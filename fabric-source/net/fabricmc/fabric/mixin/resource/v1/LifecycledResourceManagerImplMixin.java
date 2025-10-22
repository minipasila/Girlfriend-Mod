package net.fabricmc.fabric.mixin.resource.v1;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;

import net.fabricmc.fabric.impl.resource.v1.FabricLifecycledResourceManager;

@Mixin(LifecycledResourceManagerImpl.class)
public class LifecycledResourceManagerImplMixin implements FabricLifecycledResourceManager {
	@Unique
	private ResourceType resourceType;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(ResourceType resourceType, List<ResourcePack> list, CallbackInfo ci) {
		this.resourceType = resourceType;
	}

	@Override
	public ResourceType fabric$getResourceType() {
		return this.resourceType;
	}
}
