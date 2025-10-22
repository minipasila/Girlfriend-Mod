package net.fabricmc.fabric.api.registry;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * An extension of {@link BrewingRecipeRegistry.Builder} to support ingredients.
 */
public interface FabricBrewingRecipeRegistryBuilder {
	/**
	 * An event that is called when the brewing recipe registry is being built.
	 */
	Event<FabricBrewingRecipeRegistryBuilder.BuildCallback> BUILD = EventFactory.createArrayBacked(FabricBrewingRecipeRegistryBuilder.BuildCallback.class, listeners -> builder -> {
		for (FabricBrewingRecipeRegistryBuilder.BuildCallback listener : listeners) {
			listener.build(builder);
		}
	});

	default void registerItemRecipe(Item input, Ingredient ingredient, Item output) {
		throw new AssertionError("Must be implemented via interface injection");
	}

	default void registerPotionRecipe(RegistryEntry<Potion> input, Ingredient ingredient, RegistryEntry<Potion> output) {
		throw new AssertionError("Must be implemented via interface injection");
	}

	default void registerRecipes(Ingredient ingredient, RegistryEntry<Potion> potion) {
		throw new AssertionError("Must be implemented via interface injection");
	}

	default FeatureSet getEnabledFeatures() {
		throw new AssertionError("Must be implemented via interface injection");
	}

	/**
	 * Use this event to register custom brewing recipes.
	 */
	@FunctionalInterface
	interface BuildCallback {
		/**
		 * Called when the brewing recipe registry is being built.
		 *
		 * @param builder the {@link BrewingRecipeRegistry} instance
		 */
		void build(BrewingRecipeRegistry.Builder builder);
	}
}
