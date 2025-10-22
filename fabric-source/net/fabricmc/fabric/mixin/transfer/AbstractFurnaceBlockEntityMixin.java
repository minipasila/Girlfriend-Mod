package net.fabricmc.fabric.mixin.transfer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;

/**
 * Defer cook time updates for furnaces, so that aborted transactions don't reset the cook time.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends LockableContainerBlockEntity implements SpecialLogicInventory {
	@Shadow
	protected DefaultedList<ItemStack> inventory;
	@Shadow
	int cookingTimeSpent;
	@Shadow
	int cookingTotalTime;
	@Unique
	private boolean fabric_suppressSpecialLogic = false;

	protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
		throw new AssertionError();
	}

	@Inject(at = @At("HEAD"), method = "setStack", cancellable = true)
	public void setStackSuppressUpdate(int slot, ItemStack stack, CallbackInfo ci) {
		if (fabric_suppressSpecialLogic) {
			inventory.set(slot, stack);
			ci.cancel();
		}
	}

	@Override
	public void fabric_setSuppress(boolean suppress) {
		fabric_suppressSpecialLogic = suppress;
	}

	@Override
	public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
		if (slot == 0) {
			ItemStack itemStack = oldStack;
			ItemStack stack = newStack;

			// Update cook time if needed. Code taken from AbstractFurnaceBlockEntity#setStack.
			boolean bl = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, itemStack);

			if (!bl && this.world instanceof ServerWorld world) {
				this.cookingTotalTime = getCookTime(world, (AbstractFurnaceBlockEntity) (Object) this);
				this.cookingTimeSpent = 0;
			}
		}
	}

	@Shadow
	private static int getCookTime(ServerWorld world, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
		throw new AssertionError();
	}
}
