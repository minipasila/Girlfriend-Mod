package net.fabricmc.fabric.impl.datagen;

import java.util.function.Predicate;

import net.minecraft.registry.tag.TagEntry;
import net.minecraft.util.Identifier;

public class ForcedTagEntry extends TagEntry {
	public ForcedTagEntry(Identifier id) {
		super(id, true, true); // if it's optional, then no need to force
	}

	@Override
	public boolean canAdd(Predicate<Identifier> objectExistsTest, Predicate<Identifier> tagExistsTest) {
		return true;
	}
}
