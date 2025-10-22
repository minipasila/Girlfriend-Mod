package net.fabricmc.fabric.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.particle.v1.FabricBlockStateParticleEffect;
import net.fabricmc.fabric.impl.particle.BlockStateParticleEffectExtension;
import net.fabricmc.fabric.impl.particle.ExtendedBlockStateParticleEffectPacketCodec;

@Mixin(BlockStateParticleEffect.class)
abstract class BlockStateParticleEffectMixin implements FabricBlockStateParticleEffect, BlockStateParticleEffectExtension {
	@Nullable
	@Unique
	private BlockPos blockPos;

	@Override
	@Nullable
	public BlockPos getBlockPos() {
		return blockPos;
	}

	@Override
	public void fabric_setBlockPos(@Nullable BlockPos pos) {
		blockPos = pos;
	}

	@ModifyReturnValue(method = "createPacketCodec", at = @At("RETURN"))
	private static PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect> modifyPacketCodec(PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect> codec) {
		return new ExtendedBlockStateParticleEffectPacketCodec(codec);
	}
}
