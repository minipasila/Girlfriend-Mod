package net.fabricmc.fabric.impl.registry.sync.packet;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.Deflater;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;

public abstract class RegistryPacketHandler<T extends RegistryPacketHandler.RegistrySyncPayload> {
	private int rawBufSize = 0;
	private int deflatedBufSize = 0;

	public abstract CustomPayload.Id<T> getPacketId();

	public abstract void sendPacket(Consumer<T> sender, Map<Identifier, Object2IntMap<Identifier>> registryMap);

	public abstract void receivePayload(T payload);

	public abstract int getTotalPacketReceived();

	public abstract boolean isPacketFinished();

	@Nullable
	public abstract SyncedPacketData getSyncedPacketData();

	protected final void computeBufSize(PacketByteBuf buf) {
		if (!RegistrySyncManager.DEBUG) {
			return;
		}

		final byte[] deflateBuffer = new byte[8192];
		ByteBuf byteBuf = buf.copy();
		Deflater deflater = new Deflater();

		int i = byteBuf.readableBytes();
		PacketByteBuf deflatedBuf = PacketByteBufs.create();

		if (i < 256) {
			deflatedBuf.writeVarInt(0);
			deflatedBuf.writeBytes(byteBuf);
		} else {
			byte[] bs = new byte[i];
			byteBuf.readBytes(bs);
			deflatedBuf.writeVarInt(bs.length);
			deflater.setInput(bs, 0, i);
			deflater.finish();

			while (!deflater.finished()) {
				int j = deflater.deflate(deflateBuffer);
				deflatedBuf.writeBytes(deflateBuffer, 0, j);
			}

			deflater.reset();
		}

		rawBufSize = buf.readableBytes();
		deflatedBufSize = deflatedBuf.readableBytes();
	}

	public final int getRawBufSize() {
		return rawBufSize;
	}

	public final int getDeflatedBufSize() {
		return deflatedBufSize;
	}

	public interface RegistrySyncPayload extends CustomPayload {
	}

	public record SyncedPacketData(
			Map<Identifier, Object2IntMap<Identifier>> idMap,
			Map<Identifier, EnumSet<RegistryAttribute>> attributes
	) { }
}
