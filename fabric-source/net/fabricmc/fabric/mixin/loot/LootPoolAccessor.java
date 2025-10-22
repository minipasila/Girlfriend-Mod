package net.fabricmc.fabric.mixin.loot;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;

import net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder;

/**
 * Accesses loot pool fields for {@link FabricLootPoolBuilder#copyOf(LootPool)}.
 * These are normally available in the transitive access widener module.
 */
@Mixin(LootPool.class)
public interface LootPoolAccessor {
	@Accessor("rolls")
	LootNumberProvider fabric_getRolls();

	@Accessor("bonusRolls")
	LootNumberProvider fabric_getBonusRolls();

	@Accessor("entries")
	List<LootPoolEntry> fabric_getEntries();

	@Accessor("conditions")
	List<LootCondition> fabric_getConditions();

	@Accessor("functions")
	List<LootFunction> fabric_getFunctions();
}
