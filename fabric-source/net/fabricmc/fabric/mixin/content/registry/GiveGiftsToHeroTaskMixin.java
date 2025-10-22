package net.fabricmc.fabric.mixin.content.registry;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.brain.task.GiveGiftsToHeroTask;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.village.VillagerProfession;

@Mixin(GiveGiftsToHeroTask.class)
public class GiveGiftsToHeroTaskMixin {
	@Shadow
	@Final
	@Mutable
	private static Map<VillagerProfession, RegistryKey<LootTable>> GIFTS;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void makeMutable(CallbackInfo ci) {
		GIFTS = new HashMap<>(GIFTS);
	}
}
