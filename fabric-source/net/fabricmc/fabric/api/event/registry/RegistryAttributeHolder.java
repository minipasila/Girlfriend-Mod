package net.fabricmc.fabric.api.event.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import net.fabricmc.fabric.impl.registry.sync.RegistryAttributeImpl;

public interface RegistryAttributeHolder {
	static RegistryAttributeHolder get(RegistryKey<?> registryKey) {
		return RegistryAttributeImpl.getHolder(registryKey);
	}

	static RegistryAttributeHolder get(Registry<?> registry) {
		return get(registry.getKey());
	}

	RegistryAttributeHolder addAttribute(RegistryAttribute attribute);

	boolean hasAttribute(RegistryAttribute attribute);
}
