package net.fabricmc.fabric.mixin.object.builder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin<T extends BlockEntity> implements FabricBlockEntityType {
	@Mutable
	@Shadow
	@Final
	private Set<Block> blocks;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void mutableBlocks(BlockEntityType.BlockEntityFactory<? extends T> factory, Set<Block> blocks, CallbackInfo ci) {
		if (!(this.blocks instanceof HashSet)) {
			this.blocks = new HashSet<>(this.blocks);
		}
	}

	@Override
	public void addSupportedBlock(Block block) {
		Objects.requireNonNull(block, "block");
		blocks.add(block);
	}
}
