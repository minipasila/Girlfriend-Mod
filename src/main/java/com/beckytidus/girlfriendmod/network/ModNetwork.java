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

    public record ConfigPayload(String apiKey, String model, String name, int tokens, double minP, double temperature) implements CustomPayload {
        public static final Id<ConfigPayload> ID = new Id<>(CONFIG_SYNC);
        public static final PacketCodec<PacketByteBuf, ConfigPayload> CODEC = CustomPayload.codecOf(ModNetwork::writeConfig, ModNetwork::readConfig);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ClearMemoryPayload() implements CustomPayload {
        public static final Id<ClearMemoryPayload> ID = new Id<>(CLEAR_MEMORY);
        public static final PacketCodec<PacketByteBuf, ClearMemoryPayload> CODEC = CustomPayload.codecOf((v, b) -> {}, b -> new ClearMemoryPayload());
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    private static void writeConfig(ConfigPayload p, PacketByteBuf buf) {
        buf.writeString(p.apiKey);
        buf.writeString(p.model);
        buf.writeString(p.name);
        buf.writeInt(p.tokens);
        buf.writeDouble(p.minP);
        buf.writeDouble(p.temperature);
    }

    private static ConfigPayload readConfig(PacketByteBuf buf) {
        return new ConfigPayload(
            buf.readString(), 
            buf.readString(), 
            buf.readString(), 
            buf.readInt(),
            buf.readDouble(),
            buf.readDouble()
        );
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearMemoryPayload.ID, ClearMemoryPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ModConfig config = ModConfig.get();
                config.apiKey = payload.apiKey;
                config.modelName = payload.model;
                config.customName = payload.name;
                config.maxHistoryTokens = payload.tokens;
                config.minP = payload.minP;
                config.temperature = payload.temperature;
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

        ServerPlayNetworking.registerGlobalReceiver(ClearMemoryPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                ServerWorld world = (ServerWorld) player.getEntityWorld();
                
                var entities = world.getEntitiesByClass(GirlFriendEntity.class, player.getBoundingBox().expand(100), 
                    g -> g.getOwner() == player);
                
                for (GirlFriendEntity gf : entities) {
                    gf.clearMemory();
                }
            });
        });
    }

    public static void sendConfigUpdatePacket(ModConfig config) {
        ClientPlayNetworking.send(new ConfigPayload(
            config.apiKey, 
            config.modelName, 
            config.customName, 
            config.maxHistoryTokens, 
            config.minP,
            config.temperature
        ));
    }
    
    public static void sendClearMemoryPacket() {
        ClientPlayNetworking.send(new ClearMemoryPayload());
    }
}