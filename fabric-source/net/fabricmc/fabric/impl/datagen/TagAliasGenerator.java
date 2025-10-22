package net.fabricmc.fabric.impl.datagen;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.tag.TagAliasGroup;

public final class TagAliasGenerator {
	public static String getDirectory(RegistryKey<? extends Registry<?>> registryKey) {
		String directory = "fabric/tag_aliases/";
		Identifier registryId = registryKey.getValue();

		if (!Identifier.DEFAULT_NAMESPACE.equals(registryId.getNamespace())) {
			directory += registryId.getNamespace() + '/';
		}

		return directory + registryId.getPath();
	}

	public static <T> CompletableFuture<?> writeTagAlias(DataWriter writer, DataOutput.PathResolver pathResolver, RegistryKey<? extends Registry<T>> registryRef, Identifier groupId, List<TagKey<T>> tags) {
		Path path = pathResolver.resolveJson(groupId);
		return DataProvider.writeCodecToPath(writer, TagAliasGroup.codec(registryRef), new TagAliasGroup<>(tags), path);
	}
}
