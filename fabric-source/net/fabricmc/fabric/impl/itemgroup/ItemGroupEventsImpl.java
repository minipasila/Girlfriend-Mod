package net.fabricmc.fabric.impl.itemgroup;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ItemGroupEventsImpl {
	private static final Map<RegistryKey<ItemGroup>, Event<ItemGroupEvents.ModifyEntries>> ITEM_GROUP_EVENT_MAP = new HashMap<>();

	public static Event<ItemGroupEvents.ModifyEntries> getOrCreateModifyEntriesEvent(RegistryKey<ItemGroup> registryKey) {
		return ITEM_GROUP_EVENT_MAP.computeIfAbsent(registryKey, (g -> createModifyEvent()));
	}

	@Nullable
	public static Event<ItemGroupEvents.ModifyEntries> getModifyEntriesEvent(RegistryKey<ItemGroup> registryKey) {
		return ITEM_GROUP_EVENT_MAP.get(registryKey);
	}

	private static Event<ItemGroupEvents.ModifyEntries> createModifyEvent() {
		return EventFactory.createArrayBacked(ItemGroupEvents.ModifyEntries.class, callbacks -> (entries) -> {
			for (ItemGroupEvents.ModifyEntries callback : callbacks) {
				callback.modifyEntries(entries);
			}
		});
	}
}
