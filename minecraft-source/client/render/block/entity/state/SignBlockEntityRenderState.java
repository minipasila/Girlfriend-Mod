package net.minecraft.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SignBlockEntityRenderState
extends BlockEntityRenderState {
    @Nullable
    public SignText frontText;
    @Nullable
    public SignText backText;
    public int textLineHeight;
    public int maxTextWidth;
    public boolean filterText;
    public boolean renderTextOutline;
}

