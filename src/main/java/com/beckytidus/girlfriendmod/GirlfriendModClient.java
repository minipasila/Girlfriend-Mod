package com.beckytidus.girlfriendmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.client.render.GirlFriendEntityRenderer;

public class GirlfriendModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityRegistry.GIRLFRIEND, GirlFriendEntityRenderer::new);
    }
}