package net.fabricmc.fabric.impl.datagen;

import net.minecraft.util.Identifier;

public interface FabricTagBuilder {
	void fabric_setReplace(boolean replace);

	boolean fabric_isReplaced();

	void fabric_forceAddTag(Identifier tag);
}
