package net.fabricmc.fabric.mixin.lookup;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
	@Accessor("blocks")
	Set<Block> getBlocks();
}
