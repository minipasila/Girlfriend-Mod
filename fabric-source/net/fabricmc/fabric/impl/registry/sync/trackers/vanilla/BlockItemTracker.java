package net.fabricmc.fabric.impl.registry.sync.trackers.vanilla;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

public final class BlockItemTracker implements RegistryEntryAddedCallback<Item> {
	private BlockItemTracker() { }

	public static void register(Registry<Item> registry) {
		BlockItemTracker tracker = new BlockItemTracker();
		RegistryEntryAddedCallback.event(registry).register(tracker);
	}

	@Override
	public void onEntryAdded(int rawId, Identifier id, Item object) {
		if (object instanceof BlockItem) {
			((BlockItem) object).appendBlocks(Item.BLOCK_ITEMS, object);
		}
	}
}
