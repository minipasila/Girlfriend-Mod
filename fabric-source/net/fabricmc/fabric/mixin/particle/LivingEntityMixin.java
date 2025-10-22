package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@ModifyExpressionValue(method = "fall", at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
	private BlockStateParticleEffect modifyBlockStateParticleEffect(BlockStateParticleEffect original, double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(landedPosition);
		return original;
	}
}
