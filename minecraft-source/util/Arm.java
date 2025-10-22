/*
 * External method calls:
 *   Lnet/minecraft/util/StringIdentifiable;createCodec(Ljava/util/function/Supplier;)Lnet/minecraft/util/StringIdentifiable$EnumCodec;
 *   Lnet/minecraft/util/function/ValueLists;createIndexToValueFunction(Ljava/util/function/ToIntFunction;[Ljava/lang/Object;Lnet/minecraft/util/function/ValueLists$OutOfBoundsHandling;)Ljava/util/function/IntFunction;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/util/Arm;method_36606()[Lnet/minecraft/util/Arm;
 *   Lnet/minecraft/util/Arm;values()[Lnet/minecraft/util/Arm;
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

public enum Arm implements TranslatableOption,
StringIdentifiable
{
    LEFT(0, "left", "options.mainHand.left"),
    RIGHT(1, "right", "options.mainHand.right");

    public static final Codec<Arm> CODEC;
    public static final IntFunction<Arm> BY_ID;
    private final int id;
    private final String name;
    private final String translationKey;

    private Arm(int id, String name, String translationKey) {
        this.id = id;
        this.name = name;
        this.translationKey = translationKey;
    }

    public Arm getOpposite() {
        if (this == LEFT) {
            return RIGHT;
        }
        return LEFT;
    }

    @Override
    public int getId() {
        return this.id;
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
        CODEC = StringIdentifiable.createCodec(Arm::values);
        BY_ID = ValueLists.createIndexToValueFunction(Arm::getId, Arm.values(), ValueLists.OutOfBoundsHandling.ZERO);
    }
}

