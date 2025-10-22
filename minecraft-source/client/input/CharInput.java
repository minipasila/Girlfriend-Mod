package net.minecraft.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringHelper;

@Environment(value=EnvType.CLIENT)
public record CharInput(int codepoint, int modifiers) {
    public String asString() {
        return Character.toString(this.codepoint);
    }

    public boolean isValidChar() {
        return StringHelper.isValidChar(this.codepoint);
    }
}

