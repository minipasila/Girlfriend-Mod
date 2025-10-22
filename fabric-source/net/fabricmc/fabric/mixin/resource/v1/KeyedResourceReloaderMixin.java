package net.fabricmc.fabric.mixin.resource.v1;

import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.impl.resource.v1.FabricResourceReloader;

@Mixin({
		/* public */
		ServerRecipeManager.class, ServerAdvancementLoader.class, FunctionLoader.class
		/* private */
})
public abstract class KeyedResourceReloaderMixin implements FabricResourceReloader {
	@Unique
	private Identifier id;

	@Override
	@SuppressWarnings({"ConstantConditions"})
	public Identifier fabric$getId() {
		if (this.id == null) {
			Object self = this;

			if (self instanceof ServerRecipeManager) {
				this.id = ResourceReloaderKeys.Server.RECIPES;
			} else if (self instanceof ServerAdvancementLoader) {
				this.id = ResourceReloaderKeys.Server.ADVANCEMENTS;
			} else if (self instanceof FunctionLoader) {
				this.id = ResourceReloaderKeys.Server.FUNCTIONS;
			} else {
				this.id = Identifier.ofVanilla("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT));
			}
		}

		return this.id;
	}
}
