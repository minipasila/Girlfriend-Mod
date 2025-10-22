package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(BreezeEntity.class)
abstract class BreezeEntityMixin extends HostileEntity {
	private BreezeEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@ModifyExpressionValue(method = { "addLongJumpingParticles", "addBlockParticles" }, at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
	private BlockStateParticleEffect modifyBlockStateParticleEffect(BlockStateParticleEffect original) {
		BlockPos blockPos = !getBlockStateAtPos().isAir() ? getBlockPos() : getSteppingPos();
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(blockPos);
		return original;
	}
}
