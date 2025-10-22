package net.minecraft.world.chunk;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public interface ChunkLoadMap {
    public void initSpawnPos(RegistryKey<World> var1, ChunkPos var2);

    @Nullable
    public ChunkStatus getStatus(int var1, int var2);

    public int getRadius();
}

