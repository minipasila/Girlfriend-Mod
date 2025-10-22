package net.fabricmc.fabric.mixin.serialization;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.storage.ReadView;

import net.fabricmc.fabric.api.serialization.v1.view.FabricReadView;

@Mixin(ReadView.class)
public interface ReadViewMixin extends FabricReadView {
}
