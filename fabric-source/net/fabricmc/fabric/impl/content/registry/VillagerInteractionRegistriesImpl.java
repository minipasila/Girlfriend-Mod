package net.fabricmc.fabric.impl.content.registry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;

import net.fabricmc.fabric.impl.content.registry.util.ImmutableCollectionUtils;
import net.fabricmc.fabric.mixin.content.registry.FarmerWorkTaskAccessor;
import net.fabricmc.fabric.mixin.content.registry.VillagerEntityAccessor;

public final class VillagerInteractionRegistriesImpl {
	private static final Set<Item> GATHERABLE_ITEMS = new HashSet<>();

	private VillagerInteractionRegistriesImpl() {
	}

	public static Set<Item> getCollectableRegistry() {
		return GATHERABLE_ITEMS;
	}

	public static List<Item> getCompostableRegistry() {
		return ImmutableCollectionUtils.getAsMutableList(FarmerWorkTaskAccessor::fabric_getCompostable, FarmerWorkTaskAccessor::fabric_setCompostables);
	}

	public static Map<Item, Integer> getFoodRegistry() {
		return ImmutableCollectionUtils.getAsMutableMap(() -> VillagerEntity.ITEM_FOOD_VALUES, VillagerEntityAccessor::fabric_setItemFoodValues);
	}
}
