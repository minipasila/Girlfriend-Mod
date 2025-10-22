package net.fabricmc.fabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;

@Mixin(Item.class)
public interface ItemAccessor {
	@Accessor
	@Mutable
	void setComponents(ComponentMap components);
}
