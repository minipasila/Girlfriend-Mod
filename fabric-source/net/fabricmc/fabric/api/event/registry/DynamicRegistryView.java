package net.fabricmc.fabric.api.event.registry;

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

/**
 * A view providing access to the registries that are currently being loaded. This is passed to
 * the {@link DynamicRegistrySetupCallback} event.
 *
 * @apiNote This might not contain all the registry, as the event is invoked for each layer of
 * the combined registry manager, and each layer holds different registries. For example, the biome
 * registry is not loaded in the {@link net.minecraft.registry.ServerDynamicRegistryType#DIMENSIONS}
 * layer.
 */
@ApiStatus.NonExtendable
public interface DynamicRegistryView {
	/**
	 * @return an {@link DynamicRegistryManager} instance representing the registry view
	 */
	DynamicRegistryManager asDynamicRegistryManager();

	/**
	 * @return the stream of registries that are currently being loaded
	 */
	Stream<Registry<?>> stream();

	/**
	 * Returns the registry identified by the registry key. This returns an empty optional if
	 * the key does not refer to a registry, or if the current combined registry layer being loaded
	 * does not contain the registry.
	 *
	 * @param registryRef the registry key of the registry to get
	 * @return the registry, or {@link Optional#empty()} if the registry is not currently being loaded
	 */
	<T> Optional<Registry<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef);

	/**
	 * A shortcut to register {@link RegistryEntryAddedCallback}.
	 * @param registryRef the registry key of the registry to register the event to
	 * @param callback the callback of the event
	 */
	<T> void registerEntryAdded(RegistryKey<? extends Registry<? extends T>> registryRef, RegistryEntryAddedCallback<T> callback);
}
