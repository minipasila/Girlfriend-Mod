/*
 * External method calls:
 *   Lnet/minecraft/util/function/ValueLists;createIndexToValueFunction(Ljava/util/function/ToIntFunction;[Ljava/lang/Object;Lnet/minecraft/util/function/ValueLists$OutOfBoundsHandling;)Ljava/util/function/IntFunction;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/option/AttackIndicator;method_36858()[Lnet/minecraft/client/option/AttackIndicator;
 *   Lnet/minecraft/client/option/AttackIndicator;values()[Lnet/minecraft/client/option/AttackIndicator;
 */
package net.minecraft.client.option;

import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

@Environment(value=EnvType.CLIENT)
public enum AttackIndicator implements TranslatableOption
{
    OFF(0, "options.off"),
    CROSSHAIR(1, "options.attack.crosshair"),
    HOTBAR(2, "options.attack.hotbar");

    private static final IntFunction<AttackIndicator> BY_ID;
    private final int id;
    private final String translationKey;

    private AttackIndicator(int id, String translationKey) {
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

    public static AttackIndicator byId(int id) {
        return BY_ID.apply(id);
    }

    static {
        BY_ID = ValueLists.createIndexToValueFunction(AttackIndicator::getId, AttackIndicator.values(), ValueLists.OutOfBoundsHandling.WRAP);
    }
}

