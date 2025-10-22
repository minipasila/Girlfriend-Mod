package net.fabricmc.fabric.impl.loot;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.entry.RegistryEntry;

public interface FabricLootTable {
	void fabric$setRegistryEntry(RegistryEntry<LootTable> key);
}
