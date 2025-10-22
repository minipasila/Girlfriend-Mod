package net.fabricmc.fabric.mixin.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;

import net.fabricmc.fabric.api.item.v1.FabricComponentMapBuilder;

@Mixin(ComponentMap.Builder.class)
abstract class ComponentMapBuilderMixin implements FabricComponentMapBuilder {
	@Shadow
	@Final
	private Reference2ObjectMap<ComponentType<?>, Object> components;

	@Shadow
	public abstract <T> ComponentMap.Builder add(ComponentType<T> type, @Nullable T value);

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getOrCreate(ComponentType<T> type, Supplier<@NotNull T> fallback) {
		if (!this.components.containsKey(type)) {
			T defaultValue = fallback.get();
			Objects.requireNonNull(defaultValue, "Cannot insert null values to component map builder");
			this.add(type, defaultValue);
		}

		return (T) this.components.get(type);
	}

	@Override
	public <T> List<T> getOrEmpty(ComponentType<List<T>> type) {
		// creating a new array list guarantees that the list in the map is mutable
		List<T> existing = new ArrayList<>(this.getOrCreate(type, Collections::emptyList));
		this.add(type, existing);
		return existing;
	}

	@Override
	public boolean contains(ComponentType<?> type) {
		return this.components.containsKey(type);
	}
}
