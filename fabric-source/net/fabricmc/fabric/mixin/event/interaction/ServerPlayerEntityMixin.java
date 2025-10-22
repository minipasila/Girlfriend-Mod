package net.fabricmc.fabric.mixin.event.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void onPlayerInteractEntity(Entity target, CallbackInfo info) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		ActionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.getEntityWorld(), Hand.MAIN_HAND, target, null);

		if (result != ActionResult.PASS) {
			info.cancel();
		}
	}

	@Inject(method = "getServerGameMode", at = @At("HEAD"), cancellable = true)
	public void fakePlayerGameMode(GameMode backupGameMode, CallbackInfoReturnable<GameMode> cir) {
		// Set the default game mode of the fake player to survival, regardless of the servers forced game mode.
		if ((Object) this instanceof FakePlayer) {
			cir.setReturnValue(GameMode.SURVIVAL);
		}
	}
}
