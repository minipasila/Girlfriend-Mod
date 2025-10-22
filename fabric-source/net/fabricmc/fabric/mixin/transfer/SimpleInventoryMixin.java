package net.fabricmc.fabric.mixin.transfer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;

/**
 * Defer markDirty until the outer transaction close callback when setStack is called from an inventory wrapper.
 */
@Mixin(SimpleInventory.class)
public class SimpleInventoryMixin implements SpecialLogicInventory {
	@Unique
	private boolean fabric_suppressSpecialLogic = false;

	@Redirect(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/SimpleInventory;markDirty()V"),
			method = "setStack(ILnet/minecraft/item/ItemStack;)V"
	)
	public void fabric_redirectMarkDirty(SimpleInventory self) {
		if (!fabric_suppressSpecialLogic) {
			self.markDirty();
		}
	}

	@Override
	public void fabric_setSuppress(boolean suppress) {
		fabric_suppressSpecialLogic = suppress;
	}

	@Override
	public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
	}
}
