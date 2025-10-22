package net.fabricmc.fabric.mixin.datagen.loot;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.data.loottable.EntityLootTableGenerator;

import net.fabricmc.fabric.api.datagen.v1.loot.FabricEntityLootTableGenerator;

@Mixin(EntityLootTableGenerator.class)
public class EntityLootTableGeneratorMixin implements FabricEntityLootTableGenerator {
}
