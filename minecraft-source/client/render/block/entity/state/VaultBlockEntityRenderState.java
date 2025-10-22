package net.minecraft.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VaultBlockEntityRenderState
extends BlockEntityRenderState {
    @Nullable
    public ItemStackEntityRenderState displayItemStackState;
    public float displayRotationDegrees;
}

