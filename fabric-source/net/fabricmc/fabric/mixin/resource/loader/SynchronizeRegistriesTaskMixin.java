package net.fabricmc.fabric.mixin.resource.loader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.server.network.SynchronizeRegistriesTask;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;

@Mixin(SynchronizeRegistriesTask.class)
public abstract class SynchronizeRegistriesTaskMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("SynchronizeRegistriesTaskMixin");
	@Shadow
	@Final
	private List<VersionedIdentifier> knownPacks;

	@Shadow
	protected abstract void syncRegistryAndTags(Consumer<Packet<?>> sender, Set<VersionedIdentifier> commonKnownPacks);

	@Inject(method = "onSelectKnownPacks", at = @At("HEAD"), cancellable = true)
	public void onSelectKnownPacks(List<VersionedIdentifier> clientKnownPacks, Consumer<Packet<?>> sender, CallbackInfo ci) {
		if (new HashSet<>(this.knownPacks).containsAll(clientKnownPacks)) {
			this.syncRegistryAndTags(sender, Set.copyOf(clientKnownPacks));
			ci.cancel();
		}
	}

	@Inject(method = "syncRegistryAndTags", at = @At("HEAD"))
	public void syncRegistryAndTags(Consumer<Packet<?>> sender, Set<VersionedIdentifier> commonKnownPacks, CallbackInfo ci) {
		LOGGER.debug("Synchronizing registries with common known packs: {}", commonKnownPacks);
	}

	@Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
	private void sendPacket(Consumer<Packet<?>> sender, CallbackInfo ci) {
		if (this.knownPacks.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
			LOGGER.warn("Too many knownPacks: Found {}; max {}", this.knownPacks.size(), ModResourcePackCreator.MAX_KNOWN_PACKS);
			sender.accept(new SelectKnownPacksS2CPacket(this.knownPacks.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS)));
			ci.cancel();
		}
	}
}
