package net.fabricmc.fabric.mixin.transfer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.inventory.ListInventory;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicAccess;

@Mixin(ListInventory.class)
interface ListInventoryMixin extends ListInventory, SpecialLogicAccess {
	@WrapOperation(method = "setStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/ListInventory;markDirty()V"))
	private void cancelMarkDirty(ListInventory instance, Operation<Void> original) {
		if (!this.fabric_shouldSuppressSpecialLogic()) {
			original.call(instance);
		}
	}
}
