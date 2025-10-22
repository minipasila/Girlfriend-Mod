package com.beckytidus.girlfriendmod.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;

public class EntityRegistry {
    public static EntityType<GirlFriendEntity> GIRLFRIEND;

    public static void register() {
        Identifier id = Identifier.of(GirlfriendMod.MOD_ID, "girlfriend");
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
        
        GIRLFRIEND = Registry.register(
                Registries.ENTITY_TYPE,
                id,
                FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GirlFriendEntity::new)
                        .dimensions(EntityDimensions.changing(0.9f, 1.9f))
                        .build(key)
        );
    }
}