package net.fabricmc.fabric.mixin.loot;

import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.impl.loot.FabricLootTable;
import net.fabricmc.fabric.impl.loot.LootUtil;

@Mixin(value = LootTable.class, priority = 3000 /* arbitrary, but requires mods to explicit set a priority to wrap the fabric event.*/)
class LootTableMixin implements FabricLootTable {
	/*
	 * the key of this loot table, if initialized.
	 */
	@Unique
	@Nullable
	RegistryEntry<LootTable> entry = null;

	@WrapMethod(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V")
	private void fabric$modifyDrops(LootContext context, Consumer<ItemStack> lootConsumer, Operation<Void> original) {
		if (entry == null) {
			this.entry = LootUtil.getEntryOrDirect(context.getWorld(), (LootTable) (Object) this);
		}

		List<ItemStack> list = new ObjectArrayList<>();
		original.call(context, (Consumer<ItemStack>) list::add);
		LootTableEvents.MODIFY_DROPS.invoker().modifyLootTableDrops(
				this.entry,
				context,
				list
		);
		list.forEach(lootConsumer);
	}

	@Override
	public void fabric$setRegistryEntry(RegistryEntry<LootTable> key) {
		this.entry = key;
	}
}
