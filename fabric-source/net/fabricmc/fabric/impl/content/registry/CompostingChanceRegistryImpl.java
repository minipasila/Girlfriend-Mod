package net.fabricmc.fabric.impl.content.registry;

import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;

public class CompostingChanceRegistryImpl implements CompostingChanceRegistry {
	@Override
	public Float get(ItemConvertible item) {
		return ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.getOrDefault(item.asItem(), 0.0F);
	}

	@Override
	public void add(ItemConvertible item, Float value) {
		ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(item.asItem(), value);
	}

	@Override
	public void add(TagKey<Item> tag, Float value) {
		throw new UnsupportedOperationException("Tags currently not supported!");
	}

	@Override
	public void remove(ItemConvertible item) {
		ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.removeFloat(item.asItem());
	}

	@Override
	public void remove(TagKey<Item> tag) {
		throw new UnsupportedOperationException("Tags currently not supported!");
	}

	@Override
	public void clear(ItemConvertible item) {
		throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
	}

	@Override
	public void clear(TagKey<Item> tag) {
		throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
	}
}
