package net.fabricmc.fabric.mixin.transfer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;

@Mixin(DoubleInventory.class)
public interface DoubleInventoryAccessor {
	@Accessor("first")
	Inventory fabric_getFirst();

	@Accessor("second")
	Inventory fabric_getSecond();
}
