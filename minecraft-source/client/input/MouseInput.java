package net.minecraft.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.AbstractInput;

@Environment(value=EnvType.CLIENT)
public record MouseInput(int button, int modifiers) implements AbstractInput
{
    @Override
    public int getKeycode() {
        return this.button;
    }
}

