package net.fabricmc.fabric.mixin.block;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.Block;

import net.fabricmc.fabric.api.block.v1.FabricBlock;

@Mixin(Block.class)
public class BlockMixin implements FabricBlock { }
