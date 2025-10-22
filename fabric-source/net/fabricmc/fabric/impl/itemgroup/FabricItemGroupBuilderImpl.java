package net.fabricmc.fabric.impl.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;

public final class FabricItemGroupBuilderImpl extends ItemGroup.Builder {
	private boolean hasDisplayName = false;

	public FabricItemGroupBuilderImpl() {
		// Set when building.
		super(null, -1);
	}

	@Override
	public ItemGroup.Builder displayName(Text displayName) {
		hasDisplayName = true;
		return super.displayName(displayName);
	}

	@Override
	public ItemGroup build() {
		if (!hasDisplayName) {
			throw new IllegalStateException("No display name set for ItemGroup");
		}

		return super.build();
	}
}
