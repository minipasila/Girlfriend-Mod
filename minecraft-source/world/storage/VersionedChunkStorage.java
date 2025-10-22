/*
 * External method calls:
 *   Lnet/minecraft/GameVersion;dataVersion()Lnet/minecraft/SaveVersion;
 *   Lnet/minecraft/datafixer/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/NbtCompound;II)Lnet/minecraft/nbt/NbtCompound;
 *   Lnet/minecraft/datafixer/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/NbtCompound;I)Lnet/minecraft/nbt/NbtCompound;
 *   Lnet/minecraft/nbt/NbtHelper;putDataVersion(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;
 *   Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;
 *   Lnet/minecraft/util/crash/CrashReport;addElement(Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReportSection;
 *   Lnet/minecraft/world/FeatureUpdater;create(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/world/PersistentStateManager;)Lnet/minecraft/world/FeatureUpdater;
 *   Lnet/minecraft/nbt/NbtCompound;putString(Ljava/lang/String;Ljava/lang/String;)V
 *   Lnet/minecraft/world/storage/StorageIoWorker;readChunkData(Lnet/minecraft/util/math/ChunkPos;)Ljava/util/concurrent/CompletableFuture;
 *   Lnet/minecraft/world/FeatureUpdater;markResolved(J)V
 *   Lnet/minecraft/world/storage/StorageIoWorker;completeAll(Z)Ljava/util/concurrent/CompletableFuture;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/world/storage/VersionedChunkStorage;saveContextToNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryKey;Ljava/util/Optional;)V
 *   Lnet/minecraft/world/storage/VersionedChunkStorage;removeContext(Lnet/minecraft/nbt/NbtCompound;)V
 *   Lnet/minecraft/world/storage/VersionedChunkStorage;markFeatureUpdateResolved(Lnet/minecraft/util/math/ChunkPos;)V
 */
package net.minecraft.world.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.FeatureUpdater;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.storage.NbtScannable;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.StorageKey;
import org.jetbrains.annotations.Nullable;

public class VersionedChunkStorage
implements AutoCloseable {
    public static final int FEATURE_UPDATING_VERSION = 1493;
    private final StorageIoWorker worker;
    protected final DataFixer dataFixer;
    @Nullable
    private volatile FeatureUpdater featureUpdater;

    public VersionedChunkStorage(StorageKey storageKey, Path directory, DataFixer dataFixer, boolean dsync) {
        this.dataFixer = dataFixer;
        this.worker = new StorageIoWorker(storageKey, directory, dsync);
    }

    public boolean needsBlending(ChunkPos chunkPos, int checkRadius) {
        return this.worker.needsBlending(chunkPos, checkRadius);
    }

    public NbtCompound updateChunkNbt(RegistryKey<World> worldKey, Supplier<PersistentStateManager> persistentStateManagerFactory, NbtCompound nbt, Optional<RegistryKey<MapCodec<? extends ChunkGenerator>>> generatorCodecKey) {
        int i = VersionedChunkStorage.getDataVersion(nbt);
        if (i == SharedConstants.getGameVersion().dataVersion().id()) {
            return nbt;
        }
        try {
            if (i < 1493 && (nbt = DataFixTypes.CHUNK.update(this.dataFixer, nbt, i, 1493)).getCompound("Level").flatMap(level -> level.getBoolean("hasLegacyStructureData")).orElse(false).booleanValue()) {
                FeatureUpdater lv = this.getFeatureUpdater(worldKey, persistentStateManagerFactory);
                nbt = lv.getUpdatedReferences(nbt);
            }
            VersionedChunkStorage.saveContextToNbt(nbt, worldKey, generatorCodecKey);
            nbt = DataFixTypes.CHUNK.update(this.dataFixer, nbt, Math.max(1493, i));
            VersionedChunkStorage.removeContext(nbt);
            NbtHelper.putDataVersion(nbt);
            return nbt;
        } catch (Exception exception) {
            CrashReport lv2 = CrashReport.create(exception, "Updated chunk");
            CrashReportSection lv3 = lv2.addElement("Updated chunk details");
            lv3.add("Data version", i);
            throw new CrashException(lv2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FeatureUpdater getFeatureUpdater(RegistryKey<World> worldKey, Supplier<PersistentStateManager> stateManagerGetter) {
        FeatureUpdater lv = this.featureUpdater;
        if (lv == null) {
            VersionedChunkStorage versionedChunkStorage = this;
            synchronized (versionedChunkStorage) {
                lv = this.featureUpdater;
                if (lv == null) {
                    this.featureUpdater = lv = FeatureUpdater.create(worldKey, stateManagerGetter.get());
                }
            }
        }
        return lv;
    }

    public static void saveContextToNbt(NbtCompound nbt, RegistryKey<World> worldKey, Optional<RegistryKey<MapCodec<? extends ChunkGenerator>>> generatorCodecKey) {
        NbtCompound lv = new NbtCompound();
        lv.putString("dimension", worldKey.getValue().toString());
        generatorCodecKey.ifPresent(key -> lv.putString("generator", key.getValue().toString()));
        nbt.put("__context", lv);
    }

    private static void removeContext(NbtCompound nbt) {
        nbt.remove("__context");
    }

    public static int getDataVersion(NbtCompound nbt) {
        return NbtHelper.getDataVersion(nbt, -1);
    }

    public CompletableFuture<Optional<NbtCompound>> getNbt(ChunkPos chunkPos) {
        return this.worker.readChunkData(chunkPos);
    }

    public CompletableFuture<Void> setNbt(ChunkPos chunkPos, Supplier<NbtCompound> nbtSupplier) {
        this.markFeatureUpdateResolved(chunkPos);
        return this.worker.setResult(chunkPos, nbtSupplier);
    }

    protected void markFeatureUpdateResolved(ChunkPos chunkPos) {
        if (this.featureUpdater != null) {
            this.featureUpdater.markResolved(chunkPos.toLong());
        }
    }

    public void completeAll() {
        this.worker.completeAll(true).join();
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }

    public NbtScannable getWorker() {
        return this.worker;
    }

    protected StorageKey getStorageKey() {
        return this.worker.getStorageKey();
    }
}

