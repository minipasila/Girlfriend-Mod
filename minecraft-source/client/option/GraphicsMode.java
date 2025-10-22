/*
 * External method calls:
 *   Lnet/minecraft/util/function/ValueLists;createIndexToValueFunction(Ljava/util/function/ToIntFunction;[Ljava/lang/Object;Lnet/minecraft/util/function/ValueLists$OutOfBoundsHandling;)Ljava/util/function/IntFunction;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/option/GraphicsMode;method_36861()[Lnet/minecraft/client/option/GraphicsMode;
 *   Lnet/minecraft/client/option/GraphicsMode;values()[Lnet/minecraft/client/option/GraphicsMode;
 */
package net.minecraft.client.option;

import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

@Environment(value=EnvType.CLIENT)
public enum GraphicsMode implements TranslatableOption
{
    FAST(0, "options.graphics.fast"),
    FANCY(1, "options.graphics.fancy"),
    FABULOUS(2, "options.graphics.fabulous");

    private static final IntFunction<GraphicsMode> BY_ID;
    private final int id;
    private final String translationKey;

    private GraphicsMode(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    public String toString() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "fast";
            case 1 -> "fancy";
            case 2 -> "fabulous";
        };
    }

    public static GraphicsMode byId(int id) {
        return BY_ID.apply(id);
    }

    static {
        BY_ID = ValueLists.createIndexToValueFunction(GraphicsMode::getId, GraphicsMode.values(), ValueLists.OutOfBoundsHandling.WRAP);
    }
}

