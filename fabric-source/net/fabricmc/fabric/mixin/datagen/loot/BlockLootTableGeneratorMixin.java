package net.fabricmc.fabric.mixin.datagen.loot;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.data.loottable.BlockLootTableGenerator;

import net.fabricmc.fabric.api.datagen.v1.loot.FabricBlockLootTableGenerator;

@Mixin(BlockLootTableGenerator.class)
public class BlockLootTableGeneratorMixin implements FabricBlockLootTableGenerator {
}
