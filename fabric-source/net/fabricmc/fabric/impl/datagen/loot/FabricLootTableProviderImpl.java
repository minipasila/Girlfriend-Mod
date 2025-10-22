package net.fabricmc.fabric.impl.datagen.loot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextType;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

public final class FabricLootTableProviderImpl {
	/**
	 * Shared run logic for {@link FabricBlockLootTableProvider} and {@link SimpleFabricLootTableProvider}.
	 */
	public static CompletableFuture<?> run(
			DataWriter writer,
			FabricLootTableProvider provider,
			ContextType contextType,
			FabricDataOutput fabricDataOutput,
			CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		HashMap<Identifier, LootTable> builders = Maps.newHashMap();
		HashMap<Identifier, ResourceCondition[]> conditionMap = new HashMap<>();

		return registryLookup.thenCompose(lookup -> {
			provider.accept((registryKey, builder) -> {
				ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(builder);
				conditionMap.put(registryKey.getValue(), conditions);

				if (builders.put(registryKey.getValue(), builder.type(contextType).build()) != null) {
					throw new IllegalStateException("Duplicate loot table " + registryKey.getValue());
				}
			});

			RegistryOps<JsonElement> ops = lookup.getOps(JsonOps.INSTANCE);
			final List<CompletableFuture<?>> futures = new ArrayList<>();

			for (Map.Entry<Identifier, LootTable> entry : builders.entrySet()) {
				JsonObject tableJson = (JsonObject) LootTable.CODEC.encodeStart(ops, entry.getValue()).getOrThrow(IllegalStateException::new);
				FabricDataGenHelper.addConditions(tableJson, conditionMap.remove(entry.getKey()));
				futures.add(DataProvider.writeToPath(writer, tableJson, getOutputPath(fabricDataOutput, entry.getKey())));
			}

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		});
	}

	private static Path getOutputPath(FabricDataOutput dataOutput, Identifier lootTableId) {
		return dataOutput.getResolver(RegistryKeys.LOOT_TABLE).resolveJson(lootTableId);
	}

	private FabricLootTableProviderImpl() {
	}
}
