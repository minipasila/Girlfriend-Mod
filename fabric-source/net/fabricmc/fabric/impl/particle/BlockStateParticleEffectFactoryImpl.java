package net.fabricmc.fabric.impl.particle;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;

public final class BlockStateParticleEffectFactoryImpl {
	private BlockStateParticleEffectFactoryImpl() {
	}

	public static BlockStateParticleEffect create(ParticleType<BlockStateParticleEffect> type, BlockState blockState, @Nullable BlockPos blockPos) {
		BlockStateParticleEffect effect = new BlockStateParticleEffect(type, blockState);
		((BlockStateParticleEffectExtension) effect).fabric_setBlockPos(blockPos);
		return effect;
	}
}
