package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.event.ChatEventHandler;
import com.beckytidus.girlfriendmod.network.ModNetwork;
// ... existing imports ...
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import com.beckytidus.girlfriendmod.command.GirlFriendCommand;
import com.beckytidus.girlfriendmod.interaction.EntityInteractionHandler;
import com.beckytidus.girlfriendmod.interaction.ItemUseHandler;
import com.beckytidus.girlfriendmod.event.EntityAttributeHandler;

public class GirlfriendMod implements ModInitializer {
    public static final String MOD_ID = "girlfriend-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Girlfriend Mod with AI...");
        
        ModConfig.load();
        ModNetwork.register();
        ChatEventHandler.register();
        
        EntityRegistry.register();
        ItemRegistry.register();
        EntityAttributeHandler.register();
        GirlFriendCommand.register();
        EntityInteractionHandler.register();
        ItemUseHandler.register();

        LOGGER.info("Girlfriend Mod loaded successfully!");
    }
}