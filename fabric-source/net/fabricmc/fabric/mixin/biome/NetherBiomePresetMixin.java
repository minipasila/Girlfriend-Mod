package net.fabricmc.fabric.mixin.biome;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import net.fabricmc.fabric.impl.biome.NetherBiomeData;

@Mixin(targets = "net/minecraft/world/biome/source/MultiNoiseBiomeSourceParameterList$Preset$1")
public class NetherBiomePresetMixin {
	@Inject(method = "apply", at = @At("RETURN"), cancellable = true)
	public <T> void apply(Function<RegistryKey<Biome>, T> function, CallbackInfoReturnable<MultiNoiseUtil.Entries<T>> cir) {
		cir.setReturnValue(NetherBiomeData.withModdedBiomeEntries(cir.getReturnValue(), function));
	}
}
