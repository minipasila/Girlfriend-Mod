package net.fabricmc.fabric.mixin.event.lifecycle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Inject(
			method = "onPlayerConnect",
			at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/SynchronizeRecipesS2CPacket")
	)
	private void hookOnPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData arg, CallbackInfo ci) {
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.invoker().onSyncDataPackContents(player, true);
	}

	@Inject(
			method = "onDataPacksReloaded",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/common/SynchronizeTagsS2CPacket;<init>(Ljava/util/Map;)V")
	)
	private void hookOnDataPacksReloaded(CallbackInfo ci) {
		for (ServerPlayerEntity player : ((PlayerManager) (Object) this).getPlayerList()) {
			ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.invoker().onSyncDataPackContents(player, false);
		}
	}
}
