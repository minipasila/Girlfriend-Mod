package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.world.World;
import net.minecraft.world.event.Vibrations;

import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;

@Mixin(WardenEntity.class)
abstract class WardenEntityMixin extends HostileEntity implements Vibrations {
	private WardenEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@ModifyExpressionValue(method = "addDigParticles", at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
	private BlockStateParticleEffect modifyBlockStateParticleEffect(BlockStateParticleEffect original) {
		((BlockStateParticleEffectExtension) original).fabric_setBlockPos(getSteppingPos());
		return original;
	}
}
