package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.Optional;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class OptionalCustomIngredientPacketCodec implements PacketCodec<RegistryByteBuf, Optional<Ingredient>> {
	private final PacketCodec<RegistryByteBuf, Optional<Ingredient>> fallback;

	public OptionalCustomIngredientPacketCodec(PacketCodec<RegistryByteBuf, Optional<Ingredient>> fallback) {
		this.fallback = fallback;
	}

	@Override
	public Optional<Ingredient> decode(RegistryByteBuf buf) {
		int index = buf.readerIndex();

		if (buf.readVarInt() != CustomIngredientPacketCodec.PACKET_MARKER) {
			// Reset index for vanilla's normal deserialization logic.
			buf.readerIndex(index);
			return this.fallback.decode(buf);
		}

		Identifier type = buf.readIdentifier();
		CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(type);

		if (serializer == null) {
			throw new IllegalArgumentException("Cannot deserialize custom ingredient of unknown type " + type);
		}

		return Optional.of(serializer.getPacketCodec().decode(buf).toVanilla());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void encode(RegistryByteBuf buf, Optional<Ingredient> value) {
		if (value.isEmpty()) {
			this.fallback.encode(buf, value);
			return;
		}

		CustomIngredient customIngredient = value.get().getCustomIngredient();

		if (CustomIngredientPacketCodec.shouldEncodeFallback(customIngredient)) {
			// The client doesn't support this custom ingredient, so we send the matching stacks as a regular ingredient.
			this.fallback.encode(buf, value);
			return;
		}

		// The client supports this custom ingredient, so we send it as a custom ingredient.
		buf.writeVarInt(CustomIngredientPacketCodec.PACKET_MARKER);
		buf.writeIdentifier(customIngredient.getSerializer().getIdentifier());
		PacketCodec<RegistryByteBuf, CustomIngredient> packetCodec = (PacketCodec<RegistryByteBuf, CustomIngredient>) customIngredient.getSerializer().getPacketCodec();
		packetCodec.encode(buf, customIngredient);
	}
}
