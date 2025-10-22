package net.fabricmc.fabric.impl.particle;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;

public interface BlockStateParticleEffectExtension {
	void fabric_setBlockPos(@Nullable BlockPos pos);
}
