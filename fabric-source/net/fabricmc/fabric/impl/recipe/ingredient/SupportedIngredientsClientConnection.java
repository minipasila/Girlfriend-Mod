package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.Set;

import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;

/**
 * Implemented on {@link ClientConnection} to store which custom ingredients the client supports.
 */
public interface SupportedIngredientsClientConnection {
	void fabric_setSupportedCustomIngredients(Set<Identifier> supportedCustomIngredients);

	Set<Identifier> fabric_getSupportedCustomIngredients();
}
