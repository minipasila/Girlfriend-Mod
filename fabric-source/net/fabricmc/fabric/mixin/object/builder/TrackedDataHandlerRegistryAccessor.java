package net.fabricmc.fabric.mixin.object.builder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.collection.Int2ObjectBiMap;

@Mixin(TrackedDataHandlerRegistry.class)
public interface TrackedDataHandlerRegistryAccessor {
	@Accessor("DATA_HANDLERS")
	static Int2ObjectBiMap<TrackedDataHandler<?>> fabric_getDataHandlers() {
		throw new AssertionError("Untransformed @Accessor");
	}
}
