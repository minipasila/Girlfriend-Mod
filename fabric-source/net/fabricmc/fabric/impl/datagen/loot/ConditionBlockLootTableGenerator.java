package net.fabricmc.fabric.impl.datagen.loot;

import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.data.loottable.BlockLootTableGenerator;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.featuretoggle.FeatureFlags;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.mixin.datagen.loot.BlockLootTableGeneratorAccessor;

public class ConditionBlockLootTableGenerator extends BlockLootTableGenerator {
	private final BlockLootTableGenerator parent;
	private final ResourceCondition[] conditions;

	public ConditionBlockLootTableGenerator(BlockLootTableGenerator parent, ResourceCondition[] conditions) {
		super(Collections.emptySet(), FeatureFlags.FEATURE_MANAGER.getFeatureSet(), ((BlockLootTableGeneratorAccessor) parent).getRegistries());

		this.parent = parent;
		this.conditions = conditions;
	}

	@Override
	public void generate() {
		throw new UnsupportedOperationException("generate() should not be called.");
	}

	@Override
	public void addDrop(Block block, LootTable.Builder lootTable) {
		FabricDataGenHelper.addConditions(lootTable, conditions);
		this.parent.addDrop(block, lootTable);
	}
}
