package net.fabricmc.fabric.api.networking.v1;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;

/**
 * A registry for payload types.
 */
@ApiStatus.NonExtendable
public interface PayloadTypeRegistry<B extends PacketByteBuf> {
	/**
	 * Registers a custom payload type.
	 *
	 * <p>This must be done on both the sending and receiving side, usually during mod initialization
	 * and <strong>before registering a packet handler</strong>.
	 *
	 * @param id    the id of the payload type
	 * @param codec the codec for the payload type
	 * @param <T>   the payload type
	 * @return the registered payload type
	 */
	<T extends CustomPayload> CustomPayload.Type<? super B, T> register(CustomPayload.Id<T> id, PacketCodec<? super B, T> codec);

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the client to server configuration channel.
	 */
	static PayloadTypeRegistry<PacketByteBuf> configurationC2S() {
		return PayloadTypeRegistryImpl.CONFIGURATION_C2S;
	}

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the server to client configuration channel.
	 */
	static PayloadTypeRegistry<PacketByteBuf> configurationS2C() {
		return PayloadTypeRegistryImpl.CONFIGURATION_S2C;
	}

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the client to server play channel.
	 */
	static PayloadTypeRegistry<RegistryByteBuf> playC2S() {
		return PayloadTypeRegistryImpl.PLAY_C2S;
	}

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the server to client play channel.
	 */
	static PayloadTypeRegistry<RegistryByteBuf> playS2C() {
		return PayloadTypeRegistryImpl.PLAY_S2C;
	}
}
