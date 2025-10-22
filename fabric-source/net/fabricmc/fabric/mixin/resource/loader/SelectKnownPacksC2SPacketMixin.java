package net.fabricmc.fabric.mixin.resource.loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;

@Mixin(SelectKnownPacksC2SPacket.class)
public class SelectKnownPacksC2SPacketMixin {
	@ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodecs;toList(I)Lnet/minecraft/network/codec/PacketCodec$ResultFunction;"))
	private static int setMaxKnownPacks(int constant) {
		return ModResourcePackCreator.MAX_KNOWN_PACKS;
	}
}
