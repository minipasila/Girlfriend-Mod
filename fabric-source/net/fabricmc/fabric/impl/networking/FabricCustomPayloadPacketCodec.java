package net.fabricmc.fabric.impl.networking;

import net.minecraft.network.PacketByteBuf;

public interface FabricCustomPayloadPacketCodec<B extends PacketByteBuf> {
	void fabric_setPacketCodecProvider(CustomPayloadTypeProvider<B> customPayloadTypeProvider);
}
