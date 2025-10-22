package net.fabricmc.fabric.impl.resource.loader;

import java.util.List;

import net.minecraft.registry.VersionedIdentifier;

public interface FabricOriginalKnownPacksGetter {
	/**
	 * @return the data packs known at server start
	 */
	List<VersionedIdentifier> fabric_getOriginalKnownPacks();
}
