package net.fabricmc.fabric.mixin.screenhandler;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.screen.NamedScreenHandlerFactory;

import net.fabricmc.fabric.api.screenhandler.v1.FabricScreenHandlerFactory;

@Mixin(NamedScreenHandlerFactory.class)
public interface NamedScreenHandlerFactoryMixin extends FabricScreenHandlerFactory {
}
