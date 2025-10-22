package net.fabricmc.fabric.mixin.content.registry;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.ai.brain.task.FarmerWorkTask;
import net.minecraft.item.Item;

@Mixin(FarmerWorkTask.class)
public interface FarmerWorkTaskAccessor {
	@Mutable
	@Accessor("COMPOSTABLES")
	static void fabric_setCompostables(List<Item> items) {
		throw new AssertionError("Untransformed @Accessor");
	}

	@Accessor("COMPOSTABLES")
	static List<Item> fabric_getCompostable() {
		throw new AssertionError("Untransformed @Accessor");
	}
}
