package net.fabricmc.fabric.impl.attachment.sync.s2c;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class RequestAcceptedAttachmentsPayloadS2C implements CustomPayload {
	public static final RequestAcceptedAttachmentsPayloadS2C INSTANCE = new RequestAcceptedAttachmentsPayloadS2C();
	public static final Identifier PACKET_ID = Identifier.of("fabric", "accepted_attachments_v1");
	public static final Id<RequestAcceptedAttachmentsPayloadS2C> ID = new Id<>(PACKET_ID);
	public static final PacketCodec<PacketByteBuf, RequestAcceptedAttachmentsPayloadS2C> CODEC = PacketCodec.unit(INSTANCE);

	private RequestAcceptedAttachmentsPayloadS2C() {
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
