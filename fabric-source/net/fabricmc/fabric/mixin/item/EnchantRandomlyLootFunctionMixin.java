package net.fabricmc.fabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;

@Mixin(EnchantRandomlyLootFunction.class)
abstract class EnchantRandomlyLootFunctionMixin {
	@Redirect(
			method = "method_60291",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z")
	)
	private static boolean callAllowEnchantingEvent(Enchantment enchantment, ItemStack stack, boolean bl, ItemStack itemStack, RegistryEntry<Enchantment> registryEntry) {
		return stack.canBeEnchantedWith(registryEntry, EnchantingContext.ACCEPTABLE);
	}
}
