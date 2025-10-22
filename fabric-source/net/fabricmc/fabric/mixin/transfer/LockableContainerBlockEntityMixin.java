package net.fabricmc.fabric.mixin.transfer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;

/**
 * Defer markDirty until the outer transaction close callback when setStack is called from an inventory wrapper.
 */
@Mixin(LockableContainerBlockEntity.class)
public class LockableContainerBlockEntityMixin implements SpecialLogicInventory {
	@Unique
	private boolean fabric_suppressSpecialLogic = false;

	@WrapOperation(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/LockableContainerBlockEntity;markDirty()V"),
			method = "setStack(ILnet/minecraft/item/ItemStack;)V"
	)
	public void fabric_redirectMarkDirty(LockableContainerBlockEntity instance, Operation<Void> original) {
		if (!fabric_suppressSpecialLogic) {
			original.call(instance);
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
