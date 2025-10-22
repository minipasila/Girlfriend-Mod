package net.fabricmc.fabric.mixin.tag;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

@Mixin(targets = "net.minecraft.registry.SimpleRegistry$TagLookup$2")
public interface SimpleRegistryTagLookup2Accessor<T> {
	@Accessor("field_53694")
	Map<TagKey<T>, RegistryEntryList.Named<T>> fabric_getTagMap();

	@Accessor("field_53694")
	@Mutable
	void fabric_setTagMap(Map<TagKey<T>, RegistryEntryList.Named<T>> tagMap);
}
