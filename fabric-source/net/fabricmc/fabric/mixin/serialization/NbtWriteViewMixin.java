package net.fabricmc.fabric.mixin.serialization;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtWriteView;

import net.fabricmc.fabric.api.serialization.v1.view.FabricWriteView;

@Mixin(NbtWriteView.class)
public class NbtWriteViewMixin implements FabricWriteView {
	@Shadow
	@Final
	private NbtCompound nbt;

	@Override
	public void putByteArray(String key, byte[] value) {
		this.nbt.putByteArray(key, value);
	}

	@Override
	public void putLongArray(String key, long[] value) {
		this.nbt.putLongArray(key, value);
	}
}
