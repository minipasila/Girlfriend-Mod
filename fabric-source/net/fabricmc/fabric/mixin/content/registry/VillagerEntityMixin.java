package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.impl.content.registry.VillagerInteractionRegistriesImpl;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
	@WrapOperation(method = "canGather", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
	private boolean useGatherableItemsSet(ItemStack stack, TagKey<Item> tag, Operation<Boolean> original) {
		return VillagerInteractionRegistriesImpl.getCollectableRegistry().contains(stack.getItem()) || original.call(stack, tag);
	}
}
