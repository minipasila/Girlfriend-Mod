package net.minecraft.client.render.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public interface Baker {
    public BakedSimpleModel getModel(Identifier var1);

    public ErrorCollectingSpriteGetter getSpriteGetter();

    public <T> T compute(ResolvableCacheKey<T> var1);

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface ResolvableCacheKey<T> {
        public T compute(Baker var1);
    }
}

