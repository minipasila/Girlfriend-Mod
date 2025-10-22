package net.fabricmc.fabric.impl.resource.loader;

import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record BuiltinModResourcePackSource(String modId) implements ResourcePackSource {
	@Override
	public boolean canBeEnabledLater() {
		return true;
	}

	@Override
	public Text decorate(Text packName) {
		return Text.translatable("pack.nameAndSource", packName, Text.translatable("pack.source.builtinMod", modId)).formatted(Formatting.GRAY);
	}
}
