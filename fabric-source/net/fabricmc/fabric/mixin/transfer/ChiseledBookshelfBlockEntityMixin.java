package net.fabricmc.fabric.mixin.transfer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;

/**
 * This mixin tracks the last interacted slot for transaction support, defers block state updates,
 * and allows setting empty stacks via {@link Inventory#setStack} in a transfer API context (needed for extractions).
 */
@Mixin(ChiseledBookshelfBlockEntity.class)
public class ChiseledBookshelfBlockEntityMixin implements SpecialLogicInventory {
	@Shadow
	private DefaultedList<ItemStack> heldStacks;
	@Shadow
	private int lastInteractedSlot; // last interacted slot
	@Unique
	private boolean fabric_suppressSpecialLogic = false;

	@Override
	public void fabric_setSuppress(boolean suppress) {
		fabric_suppressSpecialLogic = suppress;
	}

	@Inject(at = @At("HEAD"), method = "setStack", cancellable = true)
	public void setStackBypass(int slot, ItemStack stack, CallbackInfo ci) {
		if (fabric_suppressSpecialLogic) {
			heldStacks.set(slot, stack);
			ci.cancel();
		}
	}

	@Shadow
	private void updateState(int interactedSlot) {
		throw new AssertionError();
	}

	@Unique
	private final SnapshotParticipant<Integer> fabric_lastInteractedParticipant = new SnapshotParticipant<>() {
		@Override
		protected Integer createSnapshot() {
			return lastInteractedSlot;
		}

		@Override
		protected void readSnapshot(Integer snapshot) {
			lastInteractedSlot = snapshot;
		}

		@Override
		protected void onFinalCommit() {
			updateState(lastInteractedSlot);
		}
	};

	@Override
	public void fabric_onTransfer(int slot, TransactionContext transaction) {
		fabric_lastInteractedParticipant.updateSnapshots(transaction);
		lastInteractedSlot = slot;
	}

	@Override
	public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
	}
}
