package net.fabricmc.fabric.impl.particle;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;

public class ExtendedBlockStateParticleEffectPacketCodec implements PacketCodec<RegistryByteBuf, BlockStateParticleEffect> {
	private static final int PACKET_MARKER = -1;
	private final PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect> fallback;

	public ExtendedBlockStateParticleEffectPacketCodec(PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect> fallback) {
		this.fallback = fallback;
	}

	@Override
	public BlockStateParticleEffect decode(RegistryByteBuf buf) {
		int index = buf.readerIndex();

		if (buf.readVarInt() != PACKET_MARKER) {
			// Reset index for vanilla's normal deserialization logic.
			buf.readerIndex(index);
			return fallback.decode(buf);
		}

		BlockStateParticleEffect value = fallback.decode(buf);
		BlockPos pos = BlockPos.PACKET_CODEC.decode(buf);
		((BlockStateParticleEffectExtension) value).fabric_setBlockPos(pos);
		return value;
	}

	@Override
	public void encode(RegistryByteBuf buf, BlockStateParticleEffect value) {
		BlockPos pos = value.getBlockPos();

		if (pos == null || ExtendedBlockStateParticleEffectSync.shouldEncodeFallback(buf)) {
			fallback.encode(buf, value);
			return;
		}

		buf.writeVarInt(PACKET_MARKER);
		fallback.encode(buf, value);
		BlockPos.PACKET_CODEC.encode(buf, pos);
	}
}
