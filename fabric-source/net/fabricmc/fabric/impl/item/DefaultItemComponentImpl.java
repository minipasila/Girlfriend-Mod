package net.fabricmc.fabric.impl.item;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.mixin.item.ItemAccessor;

public class DefaultItemComponentImpl {
	public static void modifyItemComponents() {
		DefaultItemComponentEvents.MODIFY.invoker().modify(ModifyContextImpl.INSTANCE);
	}

	static class ModifyContextImpl implements DefaultItemComponentEvents.ModifyContext {
		private static final ModifyContextImpl INSTANCE = new ModifyContextImpl();

		private ModifyContextImpl() {
		}

		@Override
		public void modify(Predicate<Item> itemPredicate, BiConsumer<ComponentMap.Builder, Item> builderConsumer) {
			for (Item item : Registries.ITEM) {
				if (itemPredicate.test(item)) {
					ComponentMap.Builder builder = ComponentMap.builder().addAll(item.getComponents());
					builderConsumer.accept(builder, item);
					((ItemAccessor) item).setComponents(builder.build());
				}
			}
		}
	}
}
