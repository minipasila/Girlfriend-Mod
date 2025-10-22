package net.fabricmc.fabric.mixin.resource.loader;

import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;

import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.fabricmc.fabric.impl.resource.loader.ResourcePackSourceTracker;

/**
 * Implements resource pack source tracking (for {@link net.fabricmc.fabric.impl.resource.loader.FabricResource}).
 * {@link ResourcePack} doesn't hold a reference to its {@link ResourcePackSource}
 * so we store the source in a global tracker when the resource packs are created.
 *
 * @see ResourcePackSourceTracker
 */
@Mixin(ResourcePackProfile.class)
abstract class ResourcePackProfileMixin implements FabricResourcePackProfile {
	@Unique
	private static final Predicate<Set<String>> DEFAULT_PARENT_PREDICATE = parents -> true;
	@Unique
	private Predicate<Set<String>> parentsPredicate = DEFAULT_PARENT_PREDICATE;

	@Shadow
	public abstract ResourcePackInfo getInfo();

	@Inject(method = "createResourcePack", at = @At("RETURN"))
	private void onCreateResourcePack(CallbackInfoReturnable<ResourcePack> info) {
		ResourcePackSourceTracker.setSource(info.getReturnValue(), getInfo().source());
	}

	@Override
	public boolean fabric_isHidden() {
		return parentsPredicate != DEFAULT_PARENT_PREDICATE;
	}

	@Override
	public boolean fabric_parentsEnabled(Set<String> enabled) {
		return parentsPredicate.test(enabled);
	}

	@Override
	public void fabric_setParentsPredicate(Predicate<Set<String>> predicate) {
		this.parentsPredicate = predicate;
	}
}
