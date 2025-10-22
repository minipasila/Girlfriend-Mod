/*
 * External method calls:
 *   Lnet/minecraft/util/function/ValueLists;createIndexToValueFunction(Ljava/util/function/ToIntFunction;[Ljava/lang/Object;Lnet/minecraft/util/function/ValueLists$OutOfBoundsHandling;)Ljava/util/function/IntFunction;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/render/ChunkBuilderMode;method_38526()[Lnet/minecraft/client/render/ChunkBuilderMode;
 *   Lnet/minecraft/client/render/ChunkBuilderMode;values()[Lnet/minecraft/client/render/ChunkBuilderMode;
 */
package net.minecraft.client.render;

import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

@Environment(value=EnvType.CLIENT)
public enum ChunkBuilderMode implements TranslatableOption
{
    NONE(0, "options.prioritizeChunkUpdates.none"),
    PLAYER_AFFECTED(1, "options.prioritizeChunkUpdates.byPlayer"),
    NEARBY(2, "options.prioritizeChunkUpdates.nearby");

    private static final IntFunction<ChunkBuilderMode> BY_ID;
    private final int id;
    private final String name;

    private ChunkBuilderMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getTranslationKey() {
        return this.name;
    }

    public static ChunkBuilderMode get(int id) {
        return BY_ID.apply(id);
    }

    static {
        BY_ID = ValueLists.createIndexToValueFunction(ChunkBuilderMode::getId, ChunkBuilderMode.values(), ValueLists.OutOfBoundsHandling.WRAP);
    }
}

