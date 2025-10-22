package net.fabricmc.fabric.impl.registry.sync;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class SyncCompletePayload implements CustomPayload {
	public static final SyncCompletePayload INSTANCE = new SyncCompletePayload();
	public static final CustomPayload.Id<SyncCompletePayload> ID = new CustomPayload.Id<>(Identifier.of("fabric", "registry/sync/complete"));
	public static final PacketCodec<PacketByteBuf, SyncCompletePayload> CODEC = PacketCodec.unit(INSTANCE);

	private SyncCompletePayload() { }

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
