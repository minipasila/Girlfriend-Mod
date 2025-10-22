package net.fabricmc.fabric.impl.networking;

import net.minecraft.network.packet.Packet;

public interface PacketCallbackListener {
	/**
	 * Called after a packet has been sent.
	 *
	 * @param packet the packet
	 */
	void sent(Packet<?> packet);
}
