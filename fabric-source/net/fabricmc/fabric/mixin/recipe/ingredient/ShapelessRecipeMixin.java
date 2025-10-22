package net.fabricmc.fabric.mixin.recipe.ingredient;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;

import net.fabricmc.fabric.impl.recipe.ingredient.ShapelessMatch;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
	@Final
	@Shadow
	List<Ingredient> ingredients;
	@Unique
	private boolean fabric_requiresTesting = false;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void cacheRequiresTesting(String group, CraftingRecipeCategory category, ItemStack result, List<Ingredient> ingredients, CallbackInfo ci) {
		for (Ingredient ingredient : ingredients) {
			if (ingredient.requiresTesting()) {
				fabric_requiresTesting = true;
				break;
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z", cancellable = true)
	public void customIngredientMatch(CraftingRecipeInput recipeInput, World world, CallbackInfoReturnable<Boolean> cir) {
		if (fabric_requiresTesting) {
			List<ItemStack> nonEmptyStacks = new ArrayList<>(recipeInput.getStackCount());

			for (int i = 0; i < recipeInput.size(); ++i) {
				ItemStack stack = recipeInput.getStackInSlot(i);

				if (!stack.isEmpty()) {
					nonEmptyStacks.add(stack);
				}
			}

			cir.setReturnValue(ShapelessMatch.isMatch(nonEmptyStacks, ingredients));
		}
	}
}
