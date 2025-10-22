package net.fabricmc.fabric.mixin.event.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.entity.FakePlayer;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
	@Shadow
	private ServerPlayerEntity owner;

	@Inject(method = "setOwner", at = @At("HEAD"), cancellable = true)
	void preventOwnerOverride(ServerPlayerEntity newOwner, CallbackInfo ci) {
		if (newOwner instanceof FakePlayer) {
			// Prevent fake players with the same UUID as a real player from stealing the real player's advancement tracker.
			ci.cancel();
		}
	}

	@Inject(method = "grantCriterion", at = @At("HEAD"), cancellable = true)
	void preventGrantCriterion(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> ci) {
		if (owner instanceof FakePlayer) {
			// Prevent granting advancements to fake players.
			ci.setReturnValue(false);
		}
	}
}
