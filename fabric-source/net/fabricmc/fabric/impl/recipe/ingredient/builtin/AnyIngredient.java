package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class AnyIngredient extends CombinedIngredient {
	private static final MapCodec<AnyIngredient> CODEC = Ingredient.CODEC
			.listOf()
			.fieldOf("ingredients")
			.xmap(AnyIngredient::new, AnyIngredient::getIngredients);

	public static final CustomIngredientSerializer<AnyIngredient> SERIALIZER =
			new CombinedIngredient.Serializer<>(Identifier.of("fabric", "any"), AnyIngredient::new, CODEC);

	public AnyIngredient(List<Ingredient> ingredients) {
		super(ingredients);
	}

	@Override
	public boolean test(ItemStack stack) {
		for (Ingredient ingredient : ingredients) {
			if (ingredient.test(stack)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Stream<RegistryEntry<Item>> getMatchingItems() {
		return ingredients.stream()
				.flatMap(Ingredient::getMatchingItems);
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
