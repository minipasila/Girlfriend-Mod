package net.fabricmc.fabric.impl.event.interaction;

import io.netty.channel.ChannelFutureListener;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.impl.networking.UntrackedNetworkHandler;

public final class FakePlayerNetworkHandler extends ServerPlayNetworkHandler implements UntrackedNetworkHandler {
	private static final ClientConnection FAKE_CONNECTION = new FakeClientConnection();

	public FakePlayerNetworkHandler(ServerPlayerEntity player) {
		super(player.getEntityWorld().getServer(), FAKE_CONNECTION, player, ConnectedClientData.createDefault(player.getGameProfile(), false));
	}

	@Override
	public void send(Packet<?> packet, @Nullable ChannelFutureListener callbacks) { }

	private static final class FakeClientConnection extends ClientConnection {
		private FakeClientConnection() {
			super(NetworkSide.CLIENTBOUND);
		}
	}
}
