package net.fabricmc.fabric.api.resource.v1;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.resource.v1.ResourceLoaderImpl;

/**
 * Provides various hooks into the resource loader.
 */
@ApiStatus.NonExtendable
public interface ResourceLoader {
	/**
	 * The resource reloader store key for the registry lookup.
	 *
	 * @apiNote The registry lookup is only available in {@linkplain ResourceType#SERVER_DATA server data} resource reloaders.
	 */
	ResourceReloader.Key<RegistryWrapper.WrapperLookup> RELOADER_REGISTRY_LOOKUP_KEY = new ResourceReloader.Key<>();
	/**
	 * The resource reloader store key for the currently enabled feature set.
	 *
	 * @apiNote The feature set is only available in {@linkplain ResourceType#SERVER_DATA server data} resource reloaders.
	 */
	ResourceReloader.Key<FeatureSet> RELOADER_FEATURE_SET_KEY = new ResourceReloader.Key<>();

	static ResourceLoader get(ResourceType type) {
		return ResourceLoaderImpl.get(type);
	}

	/**
	 * Register a resource reloader for a given resource manager type.
	 *
	 * @param id the identifier of the resource reloader
	 * @param reloader the resource reloader
	 * @see #addReloaderOrdering(Identifier, Identifier)
	 */
	void registerReloader(Identifier id, ResourceReloader reloader);

	/**
	 * Requests that resource reloaders registered as the first identifier is applied before the other referenced resource reloader.
	 *
	 * <p>Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * <p>Please keep in mind that this only takes effect during the application stage!
	 *
	 * @param firstReloader  the identifier of the resource reloader that should run before the other
	 * @param secondReloader the identifier of the resource reloader that should run after the other
	 * @see net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys identifiers of Vanilla resource reloaders
	 * @see #registerReloader(Identifier, ResourceReloader) register a new resource reloader
	 */
	void addReloaderOrdering(Identifier firstReloader, Identifier secondReloader);
}
