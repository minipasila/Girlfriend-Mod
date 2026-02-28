package com.beckytidus.girlfriendmod.network;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class ModNetwork {
    public static final Identifier CONFIG_SYNC = Identifier.of("girlfriend-mod", "config_sync");
    public static final Identifier CLEAR_MEMORY = Identifier.of("girlfriend-mod", "clear_memory");

    public record ConfigPayload(
        String chutesApiKey,
        String chutesModel,
        String openRouterApiKey,
        String openRouterModel,
        String koboldCppUrl,
        String koboldCppModel,
        boolean koboldCppUseChatCompletions,
        String name,
        double minP,
        double temperature,
        String aiProvider
    ) implements CustomPayload {
        public static final Id<ConfigPayload> ID = new Id<>(CONFIG_SYNC);
        public static final PacketCodec<PacketByteBuf, ConfigPayload> CODEC = CustomPayload.codecOf(ModNetwork::writeConfig, ModNetwork::readConfig);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record ClearMemoryPayload() implements CustomPayload {
        public static final Id<ClearMemoryPayload> ID = new Id<>(CLEAR_MEMORY);
        public static final PacketCodec<PacketByteBuf, ClearMemoryPayload> CODEC = CustomPayload.codecOf(
            (payload, buf) -> {},
            buf -> new ClearMemoryPayload()
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private static void writeConfig(ConfigPayload p, PacketByteBuf buf) {
        buf.writeString(p.chutesApiKey());
        buf.writeString(p.chutesModel());
        buf.writeString(p.openRouterApiKey());
        buf.writeString(p.openRouterModel());
        buf.writeString(p.koboldCppUrl());
        buf.writeString(p.koboldCppModel());
        buf.writeBoolean(p.koboldCppUseChatCompletions());
        buf.writeString(p.name());
        buf.writeDouble(p.minP());
        buf.writeDouble(p.temperature());
        buf.writeString(p.aiProvider());
    }

    private static ConfigPayload readConfig(PacketByteBuf buf) {
        return new ConfigPayload(
            buf.readString(),
            buf.readString(),
            buf.readString(),
            buf.readString(),
            buf.readString(),
            buf.readString(),
            buf.readBoolean(),
            buf.readString(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readString()
        );
    }

    public static void register() {
        // Register payload types for play (client-to-server and server-to-client)
        PayloadTypeRegistry.playC2S().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearMemoryPayload.ID, ClearMemoryPayload.CODEC);

        // Server-side receiver for config updates from client
        ServerPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ModConfig config = ModConfig.get();
                config.chutesApiKey = payload.chutesApiKey();
                config.chutesModelName = payload.chutesModel();
                config.openRouterApiKey = payload.openRouterApiKey();
                config.openRouterModelName = payload.openRouterModel();
                config.koboldCppUrl = payload.koboldCppUrl();
                config.koboldCppModel = payload.koboldCppModel();
                config.koboldCppUseChatCompletions = payload.koboldCppUseChatCompletions();
                config.customName = payload.name();
                config.minP = payload.minP();
                config.temperature = payload.temperature();

                try {
                    config.aiProvider = ModConfig.AIProvider.valueOf(payload.aiProvider());
                } catch (IllegalArgumentException e) {
                    config.aiProvider = ModConfig.AIProvider.CHUTES;
                }

                ModConfig.save();

                // Update names of existing entities for this player
                if (context.player() != null) {
                    ServerWorld world = (ServerWorld) context.player().getEntityWorld();
                    world.getEntitiesByClass(GirlFriendEntity.class,
                        context.player().getBoundingBox().expand(200),
                        g -> g.getOwner() == context.player())
                        .forEach(g -> g.setPlayerCustomName(config.customName));
                }
            });
        });

        // Server-side receiver for clear memory requests
        ServerPlayNetworking.registerGlobalReceiver(ClearMemoryPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                if (player == null) return;

                ServerWorld world = (ServerWorld) player.getEntityWorld();

                var entities = world.getEntitiesByClass(GirlFriendEntity.class, player.getBoundingBox().expand(100),
                    g -> g.getOwner() == player);

                for (GirlFriendEntity gf : entities) {
                    gf.clearMemory();
                }
            });
        });
    }

    /**
     * Send config update from client to server
     */
    public static void sendConfigUpdatePacket(ModConfig config) {
        ClientPlayNetworking.send(new ConfigPayload(
            config.chutesApiKey,
            config.chutesModelName,
            config.openRouterApiKey,
            config.openRouterModelName,
            config.koboldCppUrl,
            config.koboldCppModel,
            config.koboldCppUseChatCompletions,
            config.customName,
            config.minP,
            config.temperature,
            config.aiProvider.name()
        ));
    }

    /**
     * Send clear memory request from client to server
     */
    public static void sendClearMemoryPacket() {
        ClientPlayNetworking.send(new ClearMemoryPayload());
    }
}
