package net.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface Targeter {
    @Nullable
    public LivingEntity getTarget();
}

