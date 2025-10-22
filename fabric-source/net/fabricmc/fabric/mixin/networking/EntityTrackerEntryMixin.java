package net.fabricmc.fabric.mixin.networking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

@Mixin(EntityTrackerEntry.class)
abstract class EntityTrackerEntryMixin {
	@Shadow
	@Final
	private Entity entity;

	@Inject(method = "startTracking", at = @At("TAIL"))
	private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
		EntityTrackingEvents.START_TRACKING.invoker().onStartTracking(this.entity, player);
	}

	@Inject(method = "stopTracking", at = @At("HEAD"))
	private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
		EntityTrackingEvents.STOP_TRACKING.invoker().onStopTracking(this.entity, player);
	}
}
