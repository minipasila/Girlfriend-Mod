package net.fabricmc.fabric.api.datagen.v1.loot;

import com.google.common.base.Preconditions;

import net.minecraft.data.loottable.BlockLootTableGenerator;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.loot.ConditionBlockLootTableGenerator;

/**
 * Fabric-provided extensions for {@link BlockLootTableGenerator}.
 *
 * <p>Note: This interface is automatically implemented via Mixin and interface injection.
 */
public interface FabricBlockLootTableGenerator {
	/**
	 * Return a new generator that applies the specified conditions to any loot table it receives,
	 * and then forwards the loot tables to this generator.
	 */
	default BlockLootTableGenerator withConditions(ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");

		return new ConditionBlockLootTableGenerator((BlockLootTableGenerator) this, conditions);
	}
}
