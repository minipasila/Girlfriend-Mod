package net.fabricmc.fabric.mixin.item;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntryList;

@Mixin(Enchantment.Builder.class)
public interface EnchantmentBuilderAccessor {
	@Accessor("definition")
	Enchantment.Definition getDefinition();

	@Accessor("exclusiveSet")
	RegistryEntryList<Enchantment> getExclusiveSet();

	@Accessor("effectMap")
	ComponentMap.Builder getEffectMap();

	@Invoker("getEffectsList")
	<E> List<E> invokeGetEffectsList(ComponentType<List<E>> type);
}
