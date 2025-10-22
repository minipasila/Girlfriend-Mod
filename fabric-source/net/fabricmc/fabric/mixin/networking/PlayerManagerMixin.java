package net.fabricmc.fabric.mixin.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {
	@Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/PlayerAbilitiesS2CPacket;<init>(Lnet/minecraft/entity/player/PlayerAbilities;)V"))
	private void handlePlayerConnection(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData arg, CallbackInfo ci) {
		ServerNetworkingImpl.getAddon(player.networkHandler).onClientReady();
	}
}
