package com.beckytidus.girlfriendmod.event;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.List;

public class ChatEventHandler {
    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (sender == null) return;
            
            String text = message.getContent().getString();
            ServerPlayerEntity player = sender;
            
            // 1.21.9 API Change: getWorld() -> getEntityWorld()
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            
            List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(GirlFriendEntity.class, 
                player.getBoundingBox().expand(10), 
                g -> g.getOwner() == player);
                
            if (!girlfriends.isEmpty() && !text.startsWith("/")) {
                GirlFriendEntity gf = girlfriends.get(0);
                gf.processPlayerChat(text);
            }
        });
    }
}