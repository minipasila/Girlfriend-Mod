package net.fabricmc.fabric.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.block.v1.BlockFunctionalityTags;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Inject(
			method = "canEnterTrapdoor",
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"),
			allow = 1,
			cancellable = true
	)
	private void allowTaggedBlocksForTrapdoorClimbing(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> info, @Local(ordinal = 1) BlockState belowState) {
		if (belowState.isIn(BlockFunctionalityTags.CAN_CLIMB_TRAPDOOR_ABOVE)) {
			if (belowState.getBlock() instanceof LadderBlock) {
				// Check that the ladder and trapdoor are placed in the same direction.
				// Vanilla does the same check for the normal ladder block.
				if (belowState.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING)) {
					info.setReturnValue(true);
				}
			} else {
				// Don't do any checks for other blocks. They might not even have the facing property.
				info.setReturnValue(true);
			}
		}
	}
}
