package net.fabricmc.fabric.mixin.datagen.loot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.data.loottable.EntityLootTableGenerator;
import net.minecraft.registry.RegistryWrapper;

@Mixin(EntityLootTableGenerator.class)
public interface EntityLootTableGeneratorAccessor {
	@Accessor()
	RegistryWrapper.WrapperLookup getRegistries();
}
