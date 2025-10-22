package net.fabricmc.fabric.impl.transfer.item;

import java.util.Map;

import com.google.common.collect.MapMaker;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;

/**
 * Wrapper around the cursor slot of a screen handler.
 */
public class CursorSlotWrapper extends SingleStackStorage {
	private static final Map<ScreenHandler, CursorSlotWrapper> WRAPPERS = new MapMaker().weakValues().makeMap();

	public static CursorSlotWrapper get(ScreenHandler screenHandler) {
		return WRAPPERS.computeIfAbsent(screenHandler, CursorSlotWrapper::new);
	}

	private final ScreenHandler screenHandler;

	private CursorSlotWrapper(ScreenHandler screenHandler) {
		this.screenHandler = screenHandler;
	}

	@Override
	protected ItemStack getStack() {
		return screenHandler.getCursorStack();
	}

	@Override
	protected void setStack(ItemStack stack) {
		screenHandler.setCursorStack(stack);
	}

	@Override
	public String toString() {
		return "CursorSlotWrapper[" + screenHandler + "/" + Registries.SCREEN_HANDLER.getId(screenHandler.getType()) + "]";
	}
}
