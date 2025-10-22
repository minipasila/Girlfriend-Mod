package net.fabricmc.fabric.mixin.entity.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;

@Mixin(Entity.class)
abstract class EntityMixin {
	@Shadow
	private World world;

	@WrapOperation(method = "teleportTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleportCrossDimension(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/Entity;"))
	private Entity afterWorldChanged(Entity instance, ServerWorld sourceWorld, ServerWorld targetWorld, TeleportTarget teleportTarget, Operation<Entity> original) {
		// Ret will only have an entity if the teleport worked (entity not removed, teleportTarget was valid, entity was successfully created)
		Entity ret = original.call(instance, sourceWorld, targetWorld, teleportTarget);

		if (ret != null) {
			ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.invoker().afterChangeWorld((Entity) (Object) this, ret, (ServerWorld) this.world, (ServerWorld) ret.getEntityWorld());
		}

		return ret;
	}
}
