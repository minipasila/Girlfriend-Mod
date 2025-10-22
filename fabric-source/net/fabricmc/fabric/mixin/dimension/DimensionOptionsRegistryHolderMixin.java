package net.fabricmc.fabric.mixin.dimension;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;

import net.fabricmc.fabric.impl.dimension.FailSoftMapCodec;

@Mixin(DimensionOptionsRegistryHolder.class)
public class DimensionOptionsRegistryHolderMixin {
	/**
	 * Fix the issue that cannot load world after uninstalling a dimension mod/datapack.
	 * After uninstalling a dimension mod/datapack, the dimension config in `level.dat` file cannot be deserialized.
	 * The solution is to make it fail-soft.
	 */
	@Redirect(method = "method_45516", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;group(Lcom/mojang/datafixers/kinds/App;)Lcom/mojang/datafixers/Products$P1;"))
	private static Products.P1 useFailSoftMap(RecordCodecBuilder.Instance instance, App app) {
		return instance.group(
				new FailSoftMapCodec<>(RegistryKey.createCodec(RegistryKeys.DIMENSION), DimensionOptions.CODEC)
						.fieldOf("dimensions").forGetter(DimensionOptionsRegistryHolder::dimensions)
		);
	}
}
