package net.fabricmc.fabric.impl.transfer.item;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.DebugMessages;

/**
 * Wrapper around an {@link InventorySlotWrapper}, with additional canInsert and canExtract checks.
 */
class SidedInventorySlotWrapper implements SingleSlotStorage<ItemVariant> {
	private final InventorySlotWrapper slotWrapper;
	private final SidedInventory sidedInventory;
	private final Direction direction;

	SidedInventorySlotWrapper(InventorySlotWrapper slotWrapper, SidedInventory sidedInventory, Direction direction) {
		this.slotWrapper = slotWrapper;
		this.sidedInventory = sidedInventory;
		this.direction = direction;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!sidedInventory.canInsert(slotWrapper.slot, ((ItemVariantImpl) resource).getCachedStack(), direction)) {
			return 0;
		} else {
			return slotWrapper.insert(resource, maxAmount, transaction);
		}
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!sidedInventory.canExtract(slotWrapper.slot, ((ItemVariantImpl) resource).getCachedStack(), direction)) {
			return 0;
		} else {
			return slotWrapper.extract(resource, maxAmount, transaction);
		}
	}

	@Override
	public boolean isResourceBlank() {
		return slotWrapper.isResourceBlank();
	}

	@Override
	public ItemVariant getResource() {
		return slotWrapper.getResource();
	}

	@Override
	public long getAmount() {
		return slotWrapper.getAmount();
	}

	@Override
	public long getCapacity() {
		return slotWrapper.getCapacity();
	}

	@Override
	public StorageView<ItemVariant> getUnderlyingView() {
		return slotWrapper.getUnderlyingView();
	}

	@Override
	public String toString() {
		return "SidedInventorySlotWrapper[%s#%d/%s]".formatted(DebugMessages.forInventory(sidedInventory), slotWrapper.slot, direction.name());
	}
}
