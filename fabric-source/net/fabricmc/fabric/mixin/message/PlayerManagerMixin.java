package net.fabricmc.fabric.mixin.message;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"), cancellable = true)
	private void onSendChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
		if (!ServerMessageEvents.ALLOW_CHAT_MESSAGE.invoker().allowChatMessage(message, sender, params)) {
			ci.cancel();
			return;
		}

		ServerMessageEvents.CHAT_MESSAGE.invoker().onChatMessage(message, sender, params);
	}

	@Inject(method = "broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Z)V", at = @At("HEAD"), cancellable = true)
	private void onSendGameMessage(Text message, Function<ServerPlayerEntity, Text> playerMessageFactory, boolean overlay, CallbackInfo ci) {
		if (!ServerMessageEvents.ALLOW_GAME_MESSAGE.invoker().allowGameMessage(this.server, message, overlay)) {
			ci.cancel();
			return;
		}

		ServerMessageEvents.GAME_MESSAGE.invoker().onGameMessage(this.server, message, overlay);
	}

	@Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"), cancellable = true)
	private void onSendCommandMessage(SignedMessage message, ServerCommandSource source, MessageType.Parameters params, CallbackInfo ci) {
		if (!ServerMessageEvents.ALLOW_COMMAND_MESSAGE.invoker().allowCommandMessage(message, source, params)) {
			ci.cancel();
			return;
		}

		ServerMessageEvents.COMMAND_MESSAGE.invoker().onCommandMessage(message, source, params);
	}
}
