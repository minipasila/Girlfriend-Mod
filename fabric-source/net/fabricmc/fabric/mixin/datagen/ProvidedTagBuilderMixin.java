package net.fabricmc.fabric.mixin.datagen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricProvidedTagBuilder;
import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;

/**
 * Extends ProvidedTagBuilder to support setting the replace field.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(ProvidedTagBuilder.class)
interface ProvidedTagBuilderMixin<E, T> extends FabricProvidedTagBuilder<E, T> {
	@Mixin(targets = "net.minecraft.data.tag.ProvidedTagBuilder$1")
	abstract class ProvidedTagBuilder1Mixin<E, T> implements ProvidedTagBuilderMixin<E, T> {
		// the builder param
		@Shadow
		@Final
		TagBuilder field_60483;

		@Override
		public ProvidedTagBuilder<E, T> setReplace(boolean replace) {
			((FabricTagBuilder) this.field_60483).fabric_setReplace(replace);
			return (ProvidedTagBuilder<E, T>) this;
		}

		@Override
		public ProvidedTagBuilder<E, T> forceAddTag(TagKey<T> tag) {
			((FabricTagBuilder) this.field_60483).fabric_forceAddTag(tag.id());
			return (ProvidedTagBuilder<E, T>) this;
		}
	}

	@Mixin(targets = "net.minecraft.data.tag.ProvidedTagBuilder$2")
	abstract class ProvidedTagBuilder2Mixin<E, T> implements ProvidedTagBuilderMixin<E, T> {
		// ProvidedTagBuilder.this
		@Shadow
		@Final
		ProvidedTagBuilder field_60484;

		@Override
		public ProvidedTagBuilder<E, T> setReplace(boolean replace) {
			((FabricProvidedTagBuilder) this.field_60484).setReplace(replace);
			return (ProvidedTagBuilder<E, T>) this;
		}

		@Override
		public ProvidedTagBuilder<E, T> forceAddTag(TagKey<T> tag) {
			((FabricProvidedTagBuilder) this.field_60484).forceAddTag(tag);
			return (ProvidedTagBuilder<E, T>) this;
		}
	}
}
