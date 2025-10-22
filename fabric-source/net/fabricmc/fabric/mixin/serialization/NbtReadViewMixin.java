package net.fabricmc.fabric.mixin.serialization;

import java.util.Collection;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;

import net.fabricmc.fabric.api.serialization.v1.view.FabricReadView;

@Mixin(NbtReadView.class)
public class NbtReadViewMixin implements FabricReadView {
	@Shadow
	@Final
	private NbtCompound nbt;

	@Override
	public Collection<String> keys() {
		return this.nbt.getKeys();
	}

	@Override
	public boolean contains(String key) {
		return this.nbt.contains(key);
	}

	@Override
	public Optional<byte[]> getOptionalByteArray(String key) {
		return this.nbt.getByteArray(key);
	}

	@Override
	public Optional<long[]> getOptionalLongArray(String key) {
		return this.nbt.getLongArray(key);
	}
}
