package net.fabricmc.fabric.mixin.entity.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {
	@Inject(method = "respawnPlayer", at = @At("TAIL"))
	private void afterRespawn(ServerPlayerEntity oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
		ServerPlayerEntity newPlayer = cir.getReturnValue();
		ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(oldPlayer, newPlayer, alive);

		if (oldPlayer.getEntityWorld() != newPlayer.getEntityWorld()) {
			ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.invoker().afterChangeWorld(newPlayer, oldPlayer.getEntityWorld(), newPlayer.getEntityWorld());
		}
	}

	@Inject(method = "onPlayerConnect", at = @At("RETURN"))
	private void firePlayerJoinEvent(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
		ServerPlayerEvents.JOIN.invoker().onJoin(player);
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void firePlayerLeaveEvent(ServerPlayerEntity player, CallbackInfo ci) {
		ServerPlayerEvents.LEAVE.invoker().onLeave(player);
	}
}
