package net.minecraft.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityHitboxAndView;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record EntityDebugInfo(boolean missing, double serverEntityX, double serverEntityY, double serverEntityZ, double deltaMovementX, double deltaMovementY, double deltaMovementZ, float eyeHeight, @Nullable EntityHitboxAndView hitboxes) {
    public EntityDebugInfo(boolean missing) {
        this(missing, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0f, null);
    }

    @Nullable
    public EntityHitboxAndView hitboxes() {
        return this.hitboxes;
    }
}

