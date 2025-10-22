package net.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface Attackable {
    @Nullable
    public LivingEntity getLastAttacker();
}

