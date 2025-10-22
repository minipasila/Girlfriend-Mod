package net.fabricmc.fabric.impl.registry.sync.trackers.vanilla;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.fabric.mixin.registry.sync.DebugChunkGeneratorAccessor;

public final class BlockInitTracker {
	public static void postFreeze() {
		final List<BlockState> blockStateList = Registries.BLOCK.stream()
				.flatMap((block) -> block.getStateManager().getStates().stream())
				.toList();

		final int xLength = MathHelper.ceil(MathHelper.sqrt(blockStateList.size()));
		final int zLength = MathHelper.ceil(blockStateList.size() / (float) xLength);

		DebugChunkGeneratorAccessor.setBLOCK_STATES(blockStateList);
		DebugChunkGeneratorAccessor.setX_SIDE_LENGTH(xLength);
		DebugChunkGeneratorAccessor.setZ_SIDE_LENGTH(zLength);
	}
}
