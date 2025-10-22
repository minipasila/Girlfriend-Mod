package net.fabricmc.fabric.impl.object.builder;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

public class ExtendedBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {
	@Nullable
	private final Boolean canPotentiallyExecuteCommands;

	public ExtendedBlockEntityType(BlockEntityFactory<? extends T> factory, Set<Block> blocks, @Nullable Boolean canPotentiallyExecuteCommands) {
		super(factory, blocks);
		this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
	}

	@Override
	public boolean canPotentiallyExecuteCommands() {
		if (canPotentiallyExecuteCommands != null) {
			return canPotentiallyExecuteCommands;
		}

		return super.canPotentiallyExecuteCommands();
	}
}
