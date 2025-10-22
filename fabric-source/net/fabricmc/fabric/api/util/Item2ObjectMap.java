package net.fabricmc.fabric.api.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.tag.TagKey;

public interface Item2ObjectMap<V> {
	V get(ItemConvertible item);

	void add(ItemConvertible item, V value);

	void add(TagKey<Item> tag, V value);

	void remove(ItemConvertible item);

	void remove(TagKey<Item> tag);

	void clear(ItemConvertible item);

	void clear(TagKey<Item> tag);
}
