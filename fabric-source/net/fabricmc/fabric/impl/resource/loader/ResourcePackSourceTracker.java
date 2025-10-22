package net.fabricmc.fabric.impl.resource.loader;

import java.util.WeakHashMap;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackSource;

/**
 * Tracks the sources of resource packs in a global weak hash map.
 * {@link ResourcePack} doesn't hold a reference to its {@link ResourcePackSource}
 * so we store the source in the map when the resource packs are created.
 * See {@link net.fabricmc.fabric.mixin.resource.loader.ResourcePackProfileMixin ResourcePackProfileMixin}.
 *
 * <p>The sources are later read for use in {@link FabricResource} and {@link FabricResourceImpl}.
 */
public final class ResourcePackSourceTracker {
	// Use a weak hash map so that if resource packs would be deleted, this won't keep them alive.
	private static final WeakHashMap<ResourcePack, ResourcePackSource> SOURCES = new WeakHashMap<>();

	/**
	 * Gets the source of a pack.
	 *
	 * @param pack the resource pack
	 * @return the source, or {@link ResourcePackSource#NONE} if not tracked
	 */
	public static ResourcePackSource getSource(ResourcePack pack) {
		return SOURCES.getOrDefault(pack, ResourcePackSource.NONE);
	}

	/**
	 * Sets the source of a pack.
	 *
	 * @param pack the resource pack
	 * @param source the source
	 */
	public static void setSource(ResourcePack pack, ResourcePackSource source) {
		SOURCES.put(pack, source);
	}
}
