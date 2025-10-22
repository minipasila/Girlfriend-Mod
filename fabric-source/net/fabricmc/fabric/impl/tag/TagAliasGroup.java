package net.fabricmc.fabric.impl.tag;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

/**
 * A wrapper record for tag alias groups.
 *
 * @param tags the tags in the group, must be from the same registry
 * @param <T> the type of registry entries in the tags
 */
public record TagAliasGroup<T>(List<TagKey<T>> tags) {
	/**
	 * Creates a codec for tag alias groups in the specified registry.
	 *
	 * @param registryKey the key of the registry where the tags are from
	 * @param <T>         the entry type
	 * @return the codec
	 */
	public static <T> Codec<TagAliasGroup<T>> codec(RegistryKey<? extends Registry<T>> registryKey) {
		return TagKey.unprefixedCodec(registryKey)
				.listOf()
				.fieldOf("tags")
				.xmap(TagAliasGroup::new, TagAliasGroup::tags)
				.codec();
	}
}
