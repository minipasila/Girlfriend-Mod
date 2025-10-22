package net.fabricmc.fabric.impl.content.registry;

import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.CopperBlockSet;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.HoneycombItem;

public final class OxidizableBlocksRegistryImpl {
	private OxidizableBlocksRegistryImpl() {
	}

	public static void registerOxidizableBlockPair(Block less, Block more) {
		Objects.requireNonNull(less, "Oxidizable block cannot be null!");
		Objects.requireNonNull(more, "Oxidizable block cannot be null!");
		Oxidizable.OXIDATION_LEVEL_INCREASES.get().put(less, more);
		// Fix #4371
		refreshRandomTickCache(less);
		refreshRandomTickCache(more);
	}

	public static void registerWaxableBlockPair(Block unwaxed, Block waxed) {
		Objects.requireNonNull(unwaxed, "Unwaxed block cannot be null!");
		Objects.requireNonNull(waxed, "Waxed block cannot be null!");
		HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().put(unwaxed, waxed);
	}

	public static void registerCopperBlockSet(CopperBlockSet blockSet) {
		Objects.requireNonNull(blockSet, "blockSet cannot be null!");
		blockSet.getOxidizingMap().forEach(OxidizableBlocksRegistryImpl::registerOxidizableBlockPair);
		blockSet.getWaxingMap().forEach(OxidizableBlocksRegistryImpl::registerWaxableBlockPair);
	}

	private static void refreshRandomTickCache(Block block) {
		block.getStateManager().getStates().forEach(state -> ((RandomTickCacheRefresher) state).fabric_api$refreshRandomTickCache());
	}

	public interface RandomTickCacheRefresher {
		void fabric_api$refreshRandomTickCache();
	}
}
