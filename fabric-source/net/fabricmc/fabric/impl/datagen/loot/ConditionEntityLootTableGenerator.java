package net.fabricmc.fabric.impl.datagen.loot;

import net.minecraft.data.loottable.EntityLootTableGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.mixin.datagen.loot.EntityLootTableGeneratorAccessor;

public class ConditionEntityLootTableGenerator extends EntityLootTableGenerator {
	private final EntityLootTableGenerator parent;
	private final ResourceCondition[] conditions;

	public ConditionEntityLootTableGenerator(EntityLootTableGenerator parent, ResourceCondition[] conditions) {
		super(FeatureFlags.FEATURE_MANAGER.getFeatureSet(), ((EntityLootTableGeneratorAccessor) parent).getRegistries());

		this.parent = parent;
		this.conditions = conditions;
	}

	@Override
	public void generate() {
		throw new UnsupportedOperationException("generate() should not be called.");
	}

	@Override
	public void register(EntityType<?> entityType, RegistryKey<LootTable> tableKey, LootTable.Builder lootTable) {
		FabricDataGenHelper.addConditions(lootTable, this.conditions);
		this.parent.register(entityType, tableKey, lootTable);
	}
}
