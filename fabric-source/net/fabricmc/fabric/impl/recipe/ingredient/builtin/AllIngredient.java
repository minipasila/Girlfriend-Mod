package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class AllIngredient extends CombinedIngredient {
	private static final MapCodec<AllIngredient> CODEC = Ingredient.CODEC
			.listOf()
			.fieldOf("ingredients")
			.xmap(AllIngredient::new, AllIngredient::getIngredients);

	public static final CustomIngredientSerializer<AllIngredient> SERIALIZER =
			new Serializer<>(Identifier.of("fabric", "all"), AllIngredient::new, CODEC);

	public AllIngredient(List<Ingredient> ingredients) {
		super(ingredients);
	}

	@Override
	public boolean test(ItemStack stack) {
		for (Ingredient ingredient : ingredients) {
			if (!ingredient.test(stack)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Stream<RegistryEntry<Item>> getMatchingItems() {
		// There's always at least one sub ingredient, so accessing ingredients[0] is safe.
		List<RegistryEntry<Item>> previewStacks = new ArrayList<>(ingredients.getFirst().getMatchingItems().toList());

		for (int i = 1; i < ingredients.size(); ++i) {
			Ingredient ing = ingredients.get(i);
			previewStacks.removeIf(entry -> !ing.test(entry.value().getDefaultStack()));
		}

		return previewStacks.stream();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
