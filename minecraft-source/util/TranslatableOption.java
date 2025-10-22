/*
 * External method calls:
 *   Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;
 */
package net.minecraft.util;

import net.minecraft.text.Text;

public interface TranslatableOption {
    public int getId();

    public String getTranslationKey();

    default public Text getText() {
        return Text.translatable(this.getTranslationKey());
    }
}

