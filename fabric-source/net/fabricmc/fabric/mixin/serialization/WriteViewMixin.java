package net.fabricmc.fabric.mixin.serialization;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.storage.WriteView;

import net.fabricmc.fabric.api.serialization.v1.view.FabricWriteView;

@Mixin(WriteView.class)
public interface WriteViewMixin extends FabricWriteView {
}
