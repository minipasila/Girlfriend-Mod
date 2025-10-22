package net.fabricmc.fabric.impl.content.registry;

import net.minecraft.block.BlockState;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

public interface FireBlockHooks {
	FlammableBlockRegistry.Entry fabric_getVanillaEntry(BlockState block);
}
