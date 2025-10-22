package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.nbt.NbtCompound;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

@Mixin(BannerBlockEntity.class)
abstract class BannerBlockEntityMixin {
	@ModifyExpressionValue(method = "toInitialChunkDataNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BannerBlockEntity;createNbt(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtCompound;"))
	private NbtCompound removeAttachments(NbtCompound original) {
		original.remove(AttachmentTarget.NBT_ATTACHMENT_KEY);
		return original;
	}
}
