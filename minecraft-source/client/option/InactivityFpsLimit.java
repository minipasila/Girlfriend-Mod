/*
 * External method calls:
 *   Lnet/minecraft/util/StringIdentifiable;createCodec(Ljava/util/function/Supplier;)Lnet/minecraft/util/StringIdentifiable$EnumCodec;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/option/InactivityFpsLimit;method_61961()[Lnet/minecraft/client/option/InactivityFpsLimit;
 */
package net.minecraft.client.option;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TranslatableOption;

@Environment(value=EnvType.CLIENT)
public enum InactivityFpsLimit implements TranslatableOption,
StringIdentifiable
{
    MINIMIZED(0, "minimized", "options.inactivityFpsLimit.minimized"),
    AFK(1, "afk", "options.inactivityFpsLimit.afk");

    public static final Codec<InactivityFpsLimit> Codec;
    private final int ordinal;
    private final String name;
    private final String translationKey;

    private InactivityFpsLimit(int ordinal, String name, String translationKey) {
        this.ordinal = ordinal;
        this.name = name;
        this.translationKey = translationKey;
    }

    @Override
    public int getId() {
        return this.ordinal;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public String asString() {
        return this.name;
    }

    static {
        Codec = StringIdentifiable.createCodec(InactivityFpsLimit::values);
    }
}

