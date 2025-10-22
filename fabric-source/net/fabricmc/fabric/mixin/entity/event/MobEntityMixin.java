package net.fabricmc.fabric.mixin.entity.event;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.Entity;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.mob.MobEntity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

@Mixin(MobEntity.class)
public class MobEntityMixin {
	@ModifyArg(method = "convertTo(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/conversion/EntityConversionContext;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/conversion/EntityConversionContext$Finalizer;)Lnet/minecraft/entity/mob/MobEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private Entity afterEntityConverted(Entity converted, @Local(argsOnly = true) EntityConversionContext conversionContext) {
		ServerLivingEntityEvents.MOB_CONVERSION.invoker().onConversion((MobEntity) (Object) this, (MobEntity) converted, conversionContext);
		return converted;
	}
}
