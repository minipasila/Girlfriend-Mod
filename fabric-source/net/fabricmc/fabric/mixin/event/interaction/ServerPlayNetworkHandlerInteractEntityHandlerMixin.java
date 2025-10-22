package net.fabricmc.fabric.mixin.event.interaction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

@Mixin(targets = "net/minecraft/server/network/ServerPlayNetworkHandler$1")
public abstract class ServerPlayNetworkHandlerInteractEntityHandlerMixin implements PlayerInteractEntityC2SPacket.Handler {
	@Shadow
	@Final
	ServerPlayNetworkHandler field_28963;

	@Shadow
	@Final
	Entity field_28962;

	@Inject(method = "interactAt(Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "HEAD"), cancellable = true)
	public void onPlayerInteractEntity(Hand hand, Vec3d hitPosition, CallbackInfo info) {
		PlayerEntity player = field_28963.player;
		World world = player.getEntityWorld();

		EntityHitResult hitResult = new EntityHitResult(field_28962, hitPosition.add(field_28962.getX(), field_28962.getY(), field_28962.getZ()));
		ActionResult result = UseEntityCallback.EVENT.invoker().interact(player, world, hand, field_28962, hitResult);

		if (result != ActionResult.PASS) {
			info.cancel();
		}
	}

	@Inject(method = "interact(Lnet/minecraft/util/Hand;)V", at = @At(value = "HEAD"), cancellable = true)
	public void onPlayerInteractEntity(Hand hand, CallbackInfo info) {
		PlayerEntity player = field_28963.player;
		World world = player.getEntityWorld();

		ActionResult result = UseEntityCallback.EVENT.invoker().interact(player, world, hand, field_28962, null);

		if (result != ActionResult.PASS) {
			info.cancel();
		}
	}
}
