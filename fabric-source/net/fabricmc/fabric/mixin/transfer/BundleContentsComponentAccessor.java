package net.fabricmc.fabric.mixin.transfer;

import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;

@Mixin(BundleContentsComponent.class)
public interface BundleContentsComponentAccessor {
	@Invoker("getOccupancy")
	static Fraction getOccupancy(ItemStack stack) {
		throw new AssertionError("This shouldn't happen!");
	}
}
