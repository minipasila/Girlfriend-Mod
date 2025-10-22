package net.fabricmc.fabric.api.registry;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.FuelRegistry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains events to aid in modifying fuels.
 */
@ApiStatus.NonExtendable
public interface FuelRegistryEvents {
	/**
	 * An event that is called when the fuel registry is being built after vanilla fuels have been registered and before exclusions have been applied.
	 */
	Event<FuelRegistryEvents.BuildCallback> BUILD = EventFactory.createArrayBacked(FuelRegistryEvents.BuildCallback.class, listeners -> (builder, context) -> {
		for (FuelRegistryEvents.BuildCallback listener : listeners) {
			listener.build(builder, context);
		}
	});

	/**
	 * An event that is called when the fuel registry is being built after vanilla exclusions have been applied.
	 */
	Event<FuelRegistryEvents.ExclusionsCallback> EXCLUSIONS = EventFactory.createArrayBacked(FuelRegistryEvents.ExclusionsCallback.class, listeners -> (builder, context) -> {
		for (FuelRegistryEvents.ExclusionsCallback listener : listeners) {
			listener.buildExclusions(builder, context);
		}
	});

	@ApiStatus.NonExtendable
	interface Context {
		/**
		 * Get the base smelt time for the fuel, for furnaces this defaults to 200.
		 * @return the base smelt time
		 */
		int baseSmeltTime();

		/**
		 * Get the {@link RegistryWrapper.WrapperLookup} for all registries.
		 * @return the registry lookup
		 */
		RegistryWrapper.WrapperLookup registries();

		/**
		 * Get the currently enabled feature set.
		 * @return the {@link FeatureSet} instance
		 */
		FeatureSet enabledFeatures();
	}

	/**
	 * Use this event to register custom fuels.
	 */
	@FunctionalInterface
	interface BuildCallback {
		/**
		 * Called when the fuel registry is being built after vanilla fuels have been registered and before exclusions have been applied.
		 *
		 * @param builder the builder being used to construct a {@link FuelRegistry} instance
		 * @param context the context for the event
		 */
		void build(FuelRegistry.Builder builder, Context context);
	}

	/**
	 * Use this event to register custom fuels.
	 */
	@FunctionalInterface
	interface ExclusionsCallback {
		/**
		 * Called when the fuel registry is being built after vanilla exclusions have been applied.
		 *
		 * @param builder the builder being used to construct a {@link FuelRegistry} instance
		 * @param context the context for the event
		 */
		void buildExclusions(FuelRegistry.Builder builder, Context context);
	}
}
