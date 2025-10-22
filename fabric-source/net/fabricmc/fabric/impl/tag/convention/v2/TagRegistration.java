package net.fabricmc.fabric.impl.tag.convention.v2;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;

import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;

public record TagRegistration<T>(RegistryKey<Registry<T>> registryKey) {
	public static final TagRegistration<Item> ITEM_TAG = new TagRegistration<>(RegistryKeys.ITEM);
	public static final TagRegistration<Block> BLOCK_TAG = new TagRegistration<>(RegistryKeys.BLOCK);
	public static final TagRegistration<Biome> BIOME_TAG = new TagRegistration<>(RegistryKeys.BIOME);
	public static final TagRegistration<Structure> STRUCTURE_TAG = new TagRegistration<>(RegistryKeys.STRUCTURE);
	public static final TagRegistration<Fluid> FLUID_TAG = new TagRegistration<>(RegistryKeys.FLUID);
	public static final TagRegistration<EntityType<?>> ENTITY_TYPE_TAG = new TagRegistration<>(RegistryKeys.ENTITY_TYPE);
	public static final TagRegistration<Enchantment> ENCHANTMENT_TAG = new TagRegistration<>(RegistryKeys.ENCHANTMENT);

	public TagKey<T> registerFabric(String tagId) {
		return TagKey.of(registryKey, Identifier.of(TagUtil.FABRIC_TAG_NAMESPACE, tagId));
	}

	public TagKey<T> registerC(String tagId) {
		return TagKey.of(registryKey, Identifier.of(TagUtil.C_TAG_NAMESPACE, tagId));
	}
}
