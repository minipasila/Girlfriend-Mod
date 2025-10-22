package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CollisionView;

import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;

@Mixin(PathContext.class)
public abstract class PathContextMixin {
	@Shadow
	public abstract BlockState getBlockState(BlockPos blockPos);

	@Shadow
	public abstract CollisionView getWorld();

	/**
	 * Overrides the node type for the specified position, if the position is found as neighbor block in a path.
	 */
	@Inject(method = "getNodeType", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/BlockPos$Mutable;set(III)Lnet/minecraft/util/math/BlockPos$Mutable;"), cancellable = true)
	private void onGetNodeType(int x, int y, int z, CallbackInfoReturnable<PathNodeType> cir, @Local BlockPos pos) {
		final PathNodeType neighborNodeType = LandPathNodeTypesRegistry.getPathNodeType(getBlockState(pos), getWorld(), pos, true);

		if (neighborNodeType != null) {
			cir.setReturnValue(neighborNodeType);
		}
	}
}
