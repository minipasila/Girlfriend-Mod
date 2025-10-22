package net.fabricmc.fabric.mixin.attachment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.encoding.VarInts;

@Mixin(VarInts.class)
public interface VarIntsAccessor {
	@Accessor("MAX_BYTES")
	static int getMaxByteSize() {
		throw new UnsupportedOperationException("implemented via mixin");
	}
}
