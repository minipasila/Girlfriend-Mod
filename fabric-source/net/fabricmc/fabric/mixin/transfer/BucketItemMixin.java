package net.fabricmc.fabric.mixin.transfer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.sound.SoundEvent;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;

/**
 * Automatically uses the correct bucket emptying sound for
 * fluid attributes handlers overriding {@link FluidVariantAttributeHandler#getEmptySound}.
 */
@Mixin(BucketItem.class)
public class BucketItemMixin {
	@Shadow
	@Final
	private Fluid fluid;

	@ModifyVariable(
			method = "playEmptyingSound",
			at = @At("STORE"),
			index = 4
	)
	private SoundEvent hookEmptyingSound(SoundEvent previous) {
		return FluidVariantAttributes.getHandlerOrDefault(fluid).getEmptySound(FluidVariant.of(fluid)).orElse(previous);
	}
}
