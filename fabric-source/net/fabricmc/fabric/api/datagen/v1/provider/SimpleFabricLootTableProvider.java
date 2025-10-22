package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.minecraft.data.DataWriter;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.context.ContextType;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;

/**
 * Extend this class and implement {@link #accept}. Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class SimpleFabricLootTableProvider implements FabricLootTableProvider {
	protected final FabricDataOutput output;
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup;
	protected final ContextType contextType;

	public SimpleFabricLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup, ContextType contextType) {
		this.output = output;
		this.registryLookup = registryLookup;
		this.contextType = contextType;
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return FabricLootTableProviderImpl.run(writer, this, contextType, output, registryLookup);
	}

	@Override
	public String getName() {
		return Objects.requireNonNull(LootContextTypes.MAP.inverse().get(contextType), "Could not get id for loot context type") + " Loot Table";
	}
}
