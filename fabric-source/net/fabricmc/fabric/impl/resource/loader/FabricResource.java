package net.fabricmc.fabric.impl.resource.loader;

import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourcePackSource;

/**
 * Extensions to {@link net.minecraft.resource.Resource}.
 * Automatically implemented there via a mixin.
 * Currently, this is only for use in other Fabric API modules.
 */
public interface FabricResource {
	/**
	 * Gets the resource pack source of this resource.
	 * The source is used to separate vanilla/mod resources from user resources in Fabric API.
	 *
	 * <p>Custom {@link net.minecraft.resource.Resource} implementations should override this method.
	 *
	 * @return the resource pack source
	 */
	default ResourcePackSource getFabricPackSource() {
		LoggerFactory.getLogger(FabricResource.class).error("Unknown Resource implementation {}, returning PACK_SOURCE_NONE as the source", getClass().getName());
		return ResourcePackSource.NONE;
	}
}
