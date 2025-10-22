package net.fabricmc.fabric.api.itemgroup.v1;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;

/**
 * Contains a method to create an item group builder.
 */
public final class FabricItemGroup {
	private FabricItemGroup() {
	}

	/**
	 * Creates a new builder for {@link ItemGroup}. Item groups are used to group items in the creative
	 * inventory.
	 *
	 * <p>You must register the newly created {@link ItemGroup} to the {@link Registries#ITEM_GROUP} registry.
	 *
	 * <p>You must also set a display name by calling {@link ItemGroup.Builder#displayName(Text)}
	 *
	 * <p>Example:
	 *
	 * <pre>{@code
	 * private static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MOD_ID, "test_group"));
	 *
	 * @Override
	 * public void onInitialize() {
	 *    Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
	 *       .displayName(Text.translatable("modid.test_group"))
	 *       .icon(() -> new ItemStack(Items.DIAMOND))
	 *       .entries((context, entries) -> {
	 *          entries.add(TEST_ITEM);
	 *       })
	 *       .build()
	 *    );
	 * }
	 * }</pre>
	 *
	 * @return a new {@link ItemGroup.Builder} instance
	 */
	public static ItemGroup.Builder builder() {
		return new FabricItemGroupBuilderImpl();
	}
}
