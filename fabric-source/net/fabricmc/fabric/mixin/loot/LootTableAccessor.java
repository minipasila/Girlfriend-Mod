package net.fabricmc.fabric.mixin.loot;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;

/**
 * Accesses loot table fields for {@link FabricLootTableBuilder#copyOf(LootTable)}.
 * These are normally available in the transitive access widener module.
 */
@Mixin(LootTable.class)
public interface LootTableAccessor {
	@Accessor("pools")
	List<LootPool> fabric_getPools();

	@Accessor("functions")
	List<LootFunction> fabric_getFunctions();

	@Accessor("randomSequenceId")
	Optional<Identifier> fabric_getRandomSequenceId();
}
