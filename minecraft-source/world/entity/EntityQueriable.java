package net.minecraft.world.entity;

import java.util.UUID;
import net.minecraft.world.entity.UniquelyIdentifiable;
import org.jetbrains.annotations.Nullable;

public interface EntityQueriable<IdentifiedType extends UniquelyIdentifiable> {
    @Nullable
    public IdentifiedType lookup(UUID var1);
}

