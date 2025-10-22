package net.minecraft.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MobSpawnerBlockEntityRenderState
extends BlockEntityRenderState {
    @Nullable
    public EntityRenderState displayEntityRenderState;
    public float displayEntityRotation;
    public float displayEntityScale;
}

