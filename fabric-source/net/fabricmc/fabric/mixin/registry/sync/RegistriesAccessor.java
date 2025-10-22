package net.fabricmc.fabric.mixin.registry.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registries;

@Mixin(Registries.class)
public interface RegistriesAccessor<T> {
	@Accessor()
	static MutableRegistry<MutableRegistry<?>> getROOT() {
		throw new UnsupportedOperationException();
	}
}
