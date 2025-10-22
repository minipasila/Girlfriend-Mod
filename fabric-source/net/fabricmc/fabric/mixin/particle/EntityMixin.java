package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(Entity.class)
abstract class EntityMixin {
	@ModifyExpressionValue(method = "spawnSprintingParticles", at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
	private BlockStateParticleEffect modifyBlockStateParticleEffect(BlockStateParticleEffect original, @Local(ordinal = 0) BlockPos blockPos) {
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(blockPos);
		return original;
	}
}
