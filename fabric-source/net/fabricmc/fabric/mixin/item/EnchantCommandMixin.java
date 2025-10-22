package net.fabricmc.fabric.mixin.item;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;

@Mixin(EnchantCommand.class)
abstract class EnchantCommandMixin {
	@Redirect(
			method = "execute",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z")
	)
	private static boolean callAllowEnchantingEvent(Enchantment instance, ItemStack stack, ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<Enchantment> enchantment) {
		return stack.canBeEnchantedWith(enchantment, EnchantingContext.ACCEPTABLE);
	}
}
