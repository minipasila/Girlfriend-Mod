package net.fabricmc.fabric.api.serialization.v1.view;

import net.minecraft.storage.WriteView;

import net.fabricmc.fabric.impl.serialization.SpecialCodecs;

/**
 * Fabric provided extension of WriteView.
 *
 * <p>Note: This interface is automatically implemented on {@link WriteView} via Mixin and interface injection.
 */
public interface FabricWriteView {
	default void putLongArray(String key, long[] value) {
		((WriteView) this).put(key, SpecialCodecs.LONG_ARRAY, value);
	}

	default void putByteArray(String key, byte[] value) {
		((WriteView) this).put(key, SpecialCodecs.BYTE_ARRAY, value);
	}
}
