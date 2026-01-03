package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.client.KeyInputHandler;
import com.beckytidus.girlfriendmod.client.PauseMenuIntegration;
import com.beckytidus.girlfriendmod.client.render.GirlFriendEntityRenderer;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class GirlfriendModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityRegistry.GIRLFRIEND, GirlFriendEntityRenderer::new);
        KeyInputHandler.register(); // Keep for optional G key if you want
        PauseMenuIntegration.register(); // Add pause menu integration
    }
}
