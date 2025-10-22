package com.beckytidus.girlfriendmod.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.beckytidus.girlfriendmod.item.GirlFriendSummonerItem;

public class ItemRegistry {
    public static Item GIRLFRIEND_SUMMONER;

    public static void register() {
        Identifier id = Identifier.of(GirlfriendMod.MOD_ID, "girlfriend_summoner");
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        
        // Create settings with the registry key
        Item.Settings settings = new Item.Settings()
                .maxCount(1)
                .registryKey(key);
        
        // Register IMMEDIATELY - don't store in a variable first
        GIRLFRIEND_SUMMONER = Registry.register(
            Registries.ITEM, 
            key, 
            new GirlFriendSummonerItem(settings)
        );
    }
}