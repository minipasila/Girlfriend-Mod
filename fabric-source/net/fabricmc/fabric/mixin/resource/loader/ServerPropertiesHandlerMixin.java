package net.fabricmc.fabric.mixin.resource.loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.dedicated.ServerPropertiesHandler;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;

@Mixin(ServerPropertiesHandler.class)
public class ServerPropertiesHandlerMixin {
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/resource/DataConfiguration;SAFE_MODE:Lnet/minecraft/resource/DataConfiguration;"))
	private DataConfiguration replaceDefaultDataConfiguration() {
		return ModResourcePackUtil.createDefaultDataConfiguration();
	}
}
