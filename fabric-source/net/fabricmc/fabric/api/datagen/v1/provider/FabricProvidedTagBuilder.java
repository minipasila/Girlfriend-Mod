package net.fabricmc.fabric.api.datagen.v1.provider;

import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.registry.tag.TagKey;

/**
 * Interface-injected to {@link net.minecraft.data.tag.ProvidedTagBuilder}.
 */
@SuppressWarnings("unchecked")
public interface FabricProvidedTagBuilder<E, T> {
	/**
	 * Sets the value of the {@code replace} flag. When set to {@code true}
	 * this tag will replace contents of any other tag.
	 * @param replace whether to replace the contents of the tag
	 * @return this, for chaining
	 */
	default ProvidedTagBuilder<E, T> setReplace(boolean replace) {
		return (ProvidedTagBuilder<E, T>) this;
	}

	default ProvidedTagBuilder<E, T> forceAddTag(TagKey<T> tag) {
		return (ProvidedTagBuilder<E, T>) this;
	}
}
