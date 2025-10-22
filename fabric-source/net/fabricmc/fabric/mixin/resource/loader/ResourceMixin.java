package net.fabricmc.fabric.mixin.resource.loader;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePackSource;

import net.fabricmc.fabric.impl.resource.loader.FabricResource;
import net.fabricmc.fabric.impl.resource.loader.ResourcePackSourceTracker;

/**
 * Implements {@link FabricResource} (resource source getter/setter)
 * for vanilla's basic {@link Resource} used for most game resources.
 */
@Mixin(Resource.class)
class ResourceMixin implements FabricResource {
	@SuppressWarnings("ConstantConditions")
	@Override
	public ResourcePackSource getFabricPackSource() {
		Resource self = (Resource) (Object) this;
		return ResourcePackSourceTracker.getSource(self.getPack());
	}
}
