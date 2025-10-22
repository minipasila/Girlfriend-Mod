/*
 * External method calls:
 *   Lnet/minecraft/network/PacketByteBuf;writeSyncId(I)V
 *   Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;
 *   Lnet/minecraft/network/PacketByteBuf;writeInt(I)Lnet/minecraft/network/PacketByteBuf;
 *   Lnet/minecraft/network/listener/ClientPlayPacketListener;onOpenHorseScreen(Lnet/minecraft/network/packet/s2c/play/OpenHorseScreenS2CPacket;)V
 *   Lnet/minecraft/network/packet/Packet;createCodec(Lnet/minecraft/network/codec/ValueFirstEncoder;Lnet/minecraft/network/codec/PacketDecoder;)Lnet/minecraft/network/codec/PacketCodec;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/network/packet/s2c/play/OpenHorseScreenS2CPacket;apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public class OpenHorseScreenS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, OpenHorseScreenS2CPacket> CODEC = Packet.createCodec(OpenHorseScreenS2CPacket::write, OpenHorseScreenS2CPacket::new);
    private final int syncId;
    private final int slotColumnCount;
    private final int horseId;

    public OpenHorseScreenS2CPacket(int syncId, int slotColumnCount, int horseId) {
        this.syncId = syncId;
        this.slotColumnCount = slotColumnCount;
        this.horseId = horseId;
    }

    private OpenHorseScreenS2CPacket(PacketByteBuf buf) {
        this.syncId = buf.readSyncId();
        this.slotColumnCount = buf.readVarInt();
        this.horseId = buf.readInt();
    }

    private void write(PacketByteBuf buf) {
        buf.writeSyncId(this.syncId);
        buf.writeVarInt(this.slotColumnCount);
        buf.writeInt(this.horseId);
    }

    @Override
    public PacketType<OpenHorseScreenS2CPacket> getPacketType() {
        return PlayPackets.HORSE_SCREEN_OPEN;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onOpenHorseScreen(this);
    }

    public int getSyncId() {
        return this.syncId;
    }

    public int getSlotColumnCount() {
        return this.slotColumnCount;
    }

    public int getHorseId() {
        return this.horseId;
    }
}

