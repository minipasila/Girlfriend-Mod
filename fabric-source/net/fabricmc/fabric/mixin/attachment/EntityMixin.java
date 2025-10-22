package net.fabricmc.fabric.mixin.attachment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;

@Mixin(Entity.class)
abstract class EntityMixin implements AttachmentTargetImpl {
	@Shadow
	private int id;

	@Shadow
	public abstract World getEntityWorld();

	@Inject(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomData(Lnet/minecraft/storage/ReadView;)V"),
			method = "readData"
	)
	private void readEntityAttachments(ReadView data, CallbackInfo ci) {
		this.fabric_readAttachmentsFromNbt(data);
	}

	@Inject(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomData(Lnet/minecraft/storage/WriteView;)V"),
			method = "writeData"
	)
	private void writeEntityAttachments(WriteView view, CallbackInfo ci) {
		this.fabric_writeAttachmentsToNbt(view);
	}

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return new AttachmentTargetInfo.EntityTarget(this.id);
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentSyncPayloadS2C payload) {
		if (!this.getEntityWorld().isClient()) {
			AttachmentSyncPredicate predicate = ((AttachmentTypeImpl<?>) type).syncPredicate();

			if ((Object) this instanceof ServerPlayerEntity self && predicate.test(this, self)) {
				// Players do not track themselves
				AttachmentSync.trySync(payload, self);
			}

			PlayerLookup.tracking((Entity) (Object) this)
					.forEach(player -> {
						if (predicate.test(this, player)) {
							AttachmentSync.trySync(payload, player);
						}
					});
		}
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		return !this.getEntityWorld().isClient();
	}

	@Override
	public DynamicRegistryManager fabric_getDynamicRegistryManager() {
		return this.getEntityWorld().getRegistryManager();
	}
}
