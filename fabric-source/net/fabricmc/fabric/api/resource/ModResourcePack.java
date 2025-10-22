package net.fabricmc.fabric.api.resource;

import net.minecraft.resource.ResourcePack;

import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Interface implemented by mod-provided resource packs.
 */
public interface ModResourcePack extends ResourcePack {
	/**
	 * @return The ModMetadata object associated with the mod providing this
	 * resource pack.
	 */
	ModMetadata getFabricModMetadata();

	ModResourcePack createOverlay(String overlay);
}
