package net.fabricmc.fabric.mixin.loot;

import java.util.Map;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.loot.LootDataType;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.loot.LootUtil;

@Mixin(JsonDataLoader.class)
public class JsonDataLoaderMixin {
	@Inject(method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/resource/ResourceFinder;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/resource/ResourceFinder;toResourceId(Lnet/minecraft/util/Identifier;)Lnet/minecraft/util/Identifier;", shift = At.Shift.AFTER))
	private static <T> void fillSourceMap(ResourceManager manager, ResourceFinder resourceFinder, DynamicOps<JsonElement> ops, Codec<T> codec, Map<Identifier, T> result, CallbackInfo ci, @Local Map.Entry<Identifier, Resource> entry, @Local(ordinal = 1) Identifier id) {
		final String dirName = ((ResourceFinderAccessor) resourceFinder).getDirectoryName();
		if (!LootDataType.LOOT_TABLES.registryKey().getValue().getPath().equals(dirName)) return;

		LootUtil.SOURCES.get().put(id, LootUtil.determineSource(entry.getValue()));
	}
}
