package net.fabricmc.fabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeyedValue;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.item.v1.FabricItem;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin implements FabricItem.Settings {
	@Shadow
	private RegistryKeyedValue<Item, Identifier> modelId;

	@Override
	public Item.Settings modelId(Identifier modelId) {
		this.modelId = RegistryKeyedValue.fixed(modelId);
		return FabricItem.Settings.super.modelId(modelId);
	}
}
