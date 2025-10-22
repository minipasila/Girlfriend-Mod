package net.fabricmc.fabric.mixin.resource.loader;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.ClientConnection;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;

import net.fabricmc.fabric.impl.resource.loader.FabricOriginalKnownPacksGetter;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigurationNetworkHandlerMixin extends ServerCommonNetworkHandler {
	public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
		super(server, connection, clientData);
	}

	/**
	 * Only use packs that were enabled at server start and are enabled now. This avoids a descync when packs have been
	 * enabled or disabled before the client joins. Since the server registry contents aren't reloaded, we don't want
	 * the client to use the new data pack data.
	 */
	@ModifyArg(method = "sendConfigurations", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/SynchronizeRegistriesTask;<init>(Ljava/util/List;Lnet/minecraft/registry/CombinedDynamicRegistries;)V", ordinal = 0))
	public List<VersionedIdentifier> filterKnownPacks(List<VersionedIdentifier> currentKnownPacks) {
		return ((FabricOriginalKnownPacksGetter) this.server).fabric_getOriginalKnownPacks().stream().filter(currentKnownPacks::contains).toList();
	}
}
