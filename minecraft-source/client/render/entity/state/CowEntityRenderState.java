package net.minecraft.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.passive.CowVariant;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CowEntityRenderState
extends LivingEntityRenderState {
    @Nullable
    public CowVariant variant;
}

