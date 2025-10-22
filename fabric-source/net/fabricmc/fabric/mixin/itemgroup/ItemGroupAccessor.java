package net.fabricmc.fabric.mixin.itemgroup;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.ItemGroup;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor {
	@Accessor
	@Mutable
	@Final
	void setRow(ItemGroup.Row row);

	@Accessor
	@Mutable
	@Final
	void setColumn(int column);
}
