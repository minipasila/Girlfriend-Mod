package net.fabricmc.fabric.impl.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.component.ComponentType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public final class ComponentTooltipAppenderRegistryImpl {
	private static final List<ComponentType<? extends TooltipAppender>> first = new ArrayList<>();
	private static final List<ComponentType<? extends TooltipAppender>> last = new ArrayList<>();
	private static final Map<ComponentType<?>, List<ComponentType<? extends TooltipAppender>>> before = new IdentityHashMap<>();
	private static final Map<ComponentType<?>, List<ComponentType<? extends TooltipAppender>>> after = new IdentityHashMap<>();
	private static boolean hasModdedEntries = false;

	public static void addFirst(ComponentType<? extends TooltipAppender> componentType) {
		first.add(componentType);
		onModified();
	}

	public static void addLast(ComponentType<? extends TooltipAppender> componentType) {
		last.add(componentType);
		onModified();
	}

	public static void addBefore(ComponentType<?> anchor, ComponentType<? extends TooltipAppender> componentType) {
		before.computeIfAbsent(anchor, k -> new ArrayList<>()).add(componentType);
		onModified();
	}

	public static void addAfter(ComponentType<?> anchor, ComponentType<? extends TooltipAppender> componentType) {
		after.computeIfAbsent(anchor, k -> new ArrayList<>()).add(componentType);
		onModified();
	}

	private static void onModified() {
		hasModdedEntries = true;
		VanillaTooltipAppenderOrder.load();
	}

	public static boolean hasModdedEntries() {
		return hasModdedEntries;
	}

	public static void onFirst(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer,
			TooltipType type
	) {
		Set<ComponentType<?>> cycleDetector = new HashSet<>();

		for (ComponentType<? extends TooltipAppender> componentType : first) {
			appendCustomComponentTooltip(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);
		}
	}

	public static void onLast(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer,
			TooltipType type
	) {
		Set<ComponentType<?>> cycleDetector = new HashSet<>();

		for (ComponentType<? extends TooltipAppender> componentType : last) {
			appendCustomComponentTooltip(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);
		}
	}

	public static void onBefore(
			ItemStack stack,
			ComponentType<?> componentType,
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer,
			TooltipType type,
			Set<ComponentType<?>> cycleDetector
	) {
		List<ComponentType<? extends TooltipAppender>> befores = before.get(componentType);

		if (befores != null) {
			for (ComponentType<? extends TooltipAppender> beforeComponentType : befores) {
				appendCustomComponentTooltip(stack, beforeComponentType, context, displayComponent, textConsumer, type, cycleDetector);
			}
		}
	}

	public static void onAfter(
			ItemStack stack,
			ComponentType<?> componentType,
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer,
			TooltipType type,
			Set<ComponentType<?>> cycleDetector
	) {
		List<ComponentType<? extends TooltipAppender>> afters = after.get(componentType);

		if (afters != null) {
			for (ComponentType<? extends TooltipAppender> afterComponentType : afters) {
				appendCustomComponentTooltip(stack, afterComponentType, context, displayComponent, textConsumer, type, cycleDetector);
			}
		}
	}

	private static void appendCustomComponentTooltip(
			ItemStack stack,
			ComponentType<? extends TooltipAppender> componentType,
			Item.TooltipContext context,
			TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer,
			TooltipType type,
			Set<ComponentType<?>> cycleDetector
	) {
		if (!cycleDetector.add(componentType)) {
			return;
		}

		onBefore(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);
		stack.appendComponentTooltip(componentType, context, displayComponent, textConsumer, type);
		onAfter(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);

		cycleDetector.remove(componentType);
	}
}
