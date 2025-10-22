package net.fabricmc.fabric.mixin.recipe.ingredient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.handler.EncoderHandler;
import net.minecraft.network.packet.Packet;

import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientSync;
import net.fabricmc.fabric.impl.recipe.ingredient.SupportedIngredientsClientConnection;

@Mixin(EncoderHandler.class)
public class EncoderHandlerMixin {
	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"
			),
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;Lio/netty/buffer/ByteBuf;)V"
	)
	private void capturePacketEncoder(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
		ChannelHandler channelHandler = channelHandlerContext.pipeline().get("packet_handler");

		if (channelHandler instanceof SupportedIngredientsClientConnection) {
			CustomIngredientSync.CURRENT_SUPPORTED_INGREDIENTS.set(((SupportedIngredientsClientConnection) channelHandler).fabric_getSupportedCustomIngredients());
		}
	}

	@Inject(
			at = {
					// Normal target after writing
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V",
							shift = At.Shift.AFTER,
							by = 1
					),
					// In the catch handler in case some exception was thrown
					@At(
							value = "INVOKE",
							target = "net/minecraft/network/packet/Packet.isWritingErrorSkippable()Z"
					)
			},
			method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;Lio/netty/buffer/ByteBuf;)V"
	)
	private void releasePacketEncoder(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
		CustomIngredientSync.CURRENT_SUPPORTED_INGREDIENTS.set(null);
	}
}
