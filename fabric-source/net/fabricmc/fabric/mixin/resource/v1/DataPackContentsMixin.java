package net.fabricmc.fabric.mixin.resource.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;

import net.fabricmc.fabric.impl.resource.v1.ResourceLoaderImpl;
import net.fabricmc.fabric.impl.resource.v1.SetupMarkerResourceReloader;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
	@ModifyArg(
			method = "method_58296",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/SimpleResourceReload;start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;"
			)
	)
	private static List<ResourceReloader> onSetupDataReloaders(
			List<ResourceReloader> reloaders,
			@Local(argsOnly = true) FeatureSet featureSet
	) {
		var list = new ArrayList<>(reloaders);
		list.addFirst(
				new SetupMarkerResourceReloader(
						ResourceLoaderImpl.getWrapperLookup(reloaders),
						featureSet
				)
		);
		return Collections.unmodifiableList(list);
	}
}
