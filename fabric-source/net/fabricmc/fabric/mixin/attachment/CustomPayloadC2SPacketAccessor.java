package net.fabricmc.fabric.mixin.attachment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;

@Mixin(CustomPayloadC2SPacket.class)
public interface CustomPayloadC2SPacketAccessor {
	@Accessor("MAX_PAYLOAD_SIZE")
	static int getMaxPayloadSize() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
