package net.fabricmc.fabric.mixin.resource.conditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;

import net.fabricmc.fabric.impl.resource.conditions.OverlayConditionsMetadata;

@Mixin(ResourcePackProfile.class)
public class ResourcePackProfileMixin {
	@ModifyVariable(method = "loadMetadata", at = @At("STORE"))
	private static List<String> applyOverlayConditions(List<String> overlays, @Local ResourcePack resourcePack) throws IOException {
		List<String> appliedOverlays = new ArrayList<>(overlays);
		OverlayConditionsMetadata overlayMetadata = resourcePack.parseMetadata(OverlayConditionsMetadata.SERIALIZER);

		if (overlayMetadata != null) {
			appliedOverlays.addAll(overlayMetadata.appliedOverlays());
		}

		return List.copyOf(appliedOverlays);
	}
}
