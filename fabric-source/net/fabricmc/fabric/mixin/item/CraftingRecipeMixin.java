package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;

import net.fabricmc.fabric.impl.item.RecipeRemainderHandler;

@Mixin(CraftingRecipe.class)
public interface CraftingRecipeMixin {
	@WrapOperation(method = "collectRecipeRemainders", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
	private static Item captureStack(ItemStack stack, Operation<Item> operation) {
		RecipeRemainderHandler.REMAINDER_STACK.set(stack.getRecipeRemainder());
		return operation.call(stack);
	}

	@Redirect(method = "collectRecipeRemainders", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getRecipeRemainder()Lnet/minecraft/item/ItemStack;"))
	private static ItemStack getStackRemainder(Item item) {
		ItemStack remainder = RecipeRemainderHandler.REMAINDER_STACK.get();
		RecipeRemainderHandler.REMAINDER_STACK.remove();
		return remainder;
	}
}
