package net.fabricmc.fabric.api.datagen.v1;

import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKey;

/**
 * An entry point for data generation.
 *
 * <p>In {@code fabric.mod.json}, the entrypoint is defined with {@code fabric-datagen} key.</p>
 *
 * @see FabricDataGenerator
 */
@FunctionalInterface
public interface DataGeneratorEntrypoint {
	/**
	 * Register {@link net.minecraft.data.DataProvider} with the {@link FabricDataGenerator} during this entrypoint.
	 *
	 * @param fabricDataGenerator The {@link FabricDataGenerator} instance
	 */
	void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator);

	/**
	 * Returns the mod ID of the mod the data is being generated for.
	 * A {@code null} return will run the data generator using the mod ID that registered the current entrypoint.
	 *
	 * @return a {@link String} or {@code null}
	 * @throws RuntimeException If the mod ID does not exist.
	 */
	@Nullable
	default String getEffectiveModId() {
		return null;
	}

	/**
	 * Builds a registry containing dynamic registry entries to be generated.
	 * Users should call {@link RegistryBuilder#addRegistry(RegistryKey, RegistryBuilder.BootstrapFunction)}
	 * to register a bootstrap function, which adds registry entries to be generated.
	 *
	 * <p>This is invoked asynchronously.
	 *
	 * @param registryBuilder a {@link RegistryBuilder} instance
	 */
	default void buildRegistry(RegistryBuilder registryBuilder) {
	}

	/**
	 * Provides a callback for setting the sort priority of object keys in generated JSON files.
	 * @param callback a callback for setting the sort priority for a given key
	 */
	default void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
	}
}
