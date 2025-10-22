package net.fabricmc.fabric.mixin.attachment;

import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;

@Mixin(WrapperProtoChunk.class)
abstract class WrapperProtoChunkMixin extends AttachmentTargetsMixin {
	@Shadow
	@Final
	private WorldChunk wrapped;

	@Override
	@Nullable
	public <T> T getAttached(AttachmentType<T> type) {
		return this.wrapped.getAttached(type);
	}

	@Override
	@Nullable
	public <T> T setAttached(AttachmentType<T> type, @Nullable T value) {
		return this.wrapped.setAttached(type, value);
	}

	@Override
	public boolean hasAttached(AttachmentType<?> type) {
		return this.wrapped.hasAttached(type);
	}

	@Override
	public void fabric_writeAttachmentsToNbt(WriteView view) {
		((AttachmentTargetImpl) this.wrapped).fabric_writeAttachmentsToNbt(view);
	}

	@Override
	public void fabric_readAttachmentsFromNbt(ReadView view) {
		((AttachmentTargetImpl) this.wrapped).fabric_readAttachmentsFromNbt(view);
	}

	@Override
	public boolean fabric_hasPersistentAttachments() {
		return ((AttachmentTargetImpl) this.wrapped).fabric_hasPersistentAttachments();
	}

	@Override
	public Map<AttachmentType<?>, ?> fabric_getAttachments() {
		return ((AttachmentTargetImpl) this.wrapped).fabric_getAttachments();
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		return ((AttachmentTargetImpl) wrapped).fabric_shouldTryToSync();
	}

	@Override
	public void fabric_computeInitialSyncChanges(ServerPlayerEntity player, Consumer<AttachmentChange> changeOutput) {
		((AttachmentTargetImpl) wrapped).fabric_computeInitialSyncChanges(player, changeOutput);
	}

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return ((AttachmentTargetImpl) wrapped).fabric_getSyncTargetInfo();
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentSyncPayloadS2C payload) {
		((AttachmentTargetImpl) wrapped).fabric_syncChange(type, payload);
	}

	@Override
	public void fabric_markChanged(AttachmentType<?> type) {
		((AttachmentTargetImpl) wrapped).fabric_markChanged(type);
	}

	@Override
	public DynamicRegistryManager fabric_getDynamicRegistryManager() {
		return ((AttachmentTargetImpl) wrapped).fabric_getDynamicRegistryManager();
	}
}
