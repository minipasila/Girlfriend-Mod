/*
 * Internal private/static methods:
 *   Lnet/minecraft/util/TriState;method_61347()[Lnet/minecraft/util/TriState;
 */
package net.minecraft.util;

public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;


    public boolean asBoolean(boolean fallback) {
        return switch (this.ordinal()) {
            case 0 -> true;
            case 1 -> false;
            default -> fallback;
        };
    }
}

