package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.event.AdvancementEventHandler;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept advancement grants and notify nearby girlfriends.
 */
@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    
    @Shadow
    private ServerPlayerEntity owner;
    
    @Shadow
    private java.util.Map<AdvancementEntry, AdvancementProgress> progress;
    
    /**
     * Inject at the end of the grantCriterion method to detect when an advancement criterion is granted.
     * We check if this completes the advancement.
     */
    @Inject(
        method = "grantCriterion",
        at = @At("TAIL")
    )
    private void onCriterionGranted(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (owner == null || owner.getEntityWorld().isClient()) return;
        
        // Check if this advancement is now complete
        AdvancementProgress advancementProgress = this.progress.get(advancement);
        if (advancementProgress != null && advancementProgress.isDone()) {
            // The advancement is complete - trigger girlfriend reaction
            AdvancementEventHandler.onAdvancementGranted(owner, advancement);
        }
    }
}