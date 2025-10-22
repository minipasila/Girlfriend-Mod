package net.fabricmc.fabric.mixin.content.registry;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
	@Mutable
	@Accessor("ITEM_FOOD_VALUES")
	static void fabric_setItemFoodValues(Map<Item, Integer> items) {
		throw new AssertionError("Untransformed @Accessor");
	}
}
