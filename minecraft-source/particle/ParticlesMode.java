/*
 * External method calls:
 *   Lnet/minecraft/util/function/ValueLists;createIndexToValueFunction(Ljava/util/function/ToIntFunction;[Ljava/lang/Object;Lnet/minecraft/util/function/ValueLists$OutOfBoundsHandling;)Ljava/util/function/IntFunction;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/particle/ParticlesMode;method_36865()[Lnet/minecraft/particle/ParticlesMode;
 *   Lnet/minecraft/particle/ParticlesMode;values()[Lnet/minecraft/particle/ParticlesMode;
 */
package net.minecraft.particle;

import java.util.function.IntFunction;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

public enum ParticlesMode implements TranslatableOption
{
    ALL(0, "options.particles.all"),
    DECREASED(1, "options.particles.decreased"),
    MINIMAL(2, "options.particles.minimal");

    private static final IntFunction<ParticlesMode> BY_ID;
    private final int id;
    private final String translationKey;

    private ParticlesMode(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public static ParticlesMode byId(int id) {
        return BY_ID.apply(id);
    }

    static {
        BY_ID = ValueLists.createIndexToValueFunction(ParticlesMode::getId, ParticlesMode.values(), ValueLists.OutOfBoundsHandling.WRAP);
    }
}

