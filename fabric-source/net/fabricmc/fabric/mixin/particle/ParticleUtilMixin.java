package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(ParticleUtil.class)
abstract class ParticleUtilMixin {
	@ModifyExpressionValue(method = "spawnSmashAttackParticles", at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
	private static BlockStateParticleEffect modifyBlockStateParticleEffect(BlockStateParticleEffect original, WorldAccess world, BlockPos pos, int count) {
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(pos);
		return original;
	}
}
