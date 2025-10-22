package net.fabricmc.fabric.mixin.content.registry;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.ai.brain.task.GiveGiftsToHeroTask;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.village.VillagerProfession;

@Mixin(GiveGiftsToHeroTask.class)
public interface GiveGiftsToHeroTaskAccessor {
	@Accessor("GIFTS")
	static Map<RegistryKey<VillagerProfession>, RegistryKey<LootTable>> fabric_getGifts() {
		throw new AssertionError("Untransformed @Accessor");
	}
}
