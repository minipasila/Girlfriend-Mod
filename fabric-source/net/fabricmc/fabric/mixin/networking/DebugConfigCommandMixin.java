package net.fabricmc.fabric.mixin.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.command.DebugConfigCommand;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;

@Mixin(DebugConfigCommand.class)
public class DebugConfigCommandMixin {
	// endConfiguration() does not re-run the configuration tasks. This means we loose the state such as what channels we can send on when in the play phase.
	@Redirect(method = "executeUnconfig", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationNetworkHandler;endConfiguration()V"))
	private static void sendConfigurations(ServerConfigurationNetworkHandler networkHandler) {
		networkHandler.sendConfigurations();
	}
}
