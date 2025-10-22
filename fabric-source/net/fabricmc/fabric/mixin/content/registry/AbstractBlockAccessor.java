package net.fabricmc.fabric.mixin.content.registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;

@Mixin(AbstractBlock.class)
public interface AbstractBlockAccessor {
	@Invoker
	boolean callHasRandomTicks(BlockState state);
}
