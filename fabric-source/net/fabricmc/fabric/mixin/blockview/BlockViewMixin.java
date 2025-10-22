package net.fabricmc.fabric.mixin.blockview;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.BlockView;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;

@Mixin(BlockView.class)
public interface BlockViewMixin extends FabricBlockView {
}
