package net.fabricmc.fabric.impl.item;

import net.minecraft.item.ItemStack;

public class RecipeRemainderHandler {
	public static final ThreadLocal<ItemStack> REMAINDER_STACK = new ThreadLocal<>();
}
