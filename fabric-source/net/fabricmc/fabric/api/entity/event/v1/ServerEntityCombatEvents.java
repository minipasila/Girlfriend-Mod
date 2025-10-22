package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events related to entities in combat.
 */
public final class ServerEntityCombatEvents {
	/**
	 * An event that is called after an entity is directly responsible for killing another entity.
	 *
	 * @see Entity#onKilledOther(ServerWorld, LivingEntity, DamageSource)
	 */
	public static final Event<AfterKilledOtherEntity> AFTER_KILLED_OTHER_ENTITY = EventFactory.createArrayBacked(AfterKilledOtherEntity.class, callbacks -> (world, entity, killedEntity, damageSource) -> {
		for (AfterKilledOtherEntity callback : callbacks) {
			callback.afterKilledOtherEntity(world, entity, killedEntity, damageSource);
		}
	});

	@FunctionalInterface
	public interface AfterKilledOtherEntity {
		/**
		 * Called after an entity has killed another entity.
		 *
		 * @param world the world
		 * @param entity the entity
		 * @param killedEntity the entity which was killed by the {@code entity}
		 * @param damageSource the damage source that killed the entity
		 */
		void afterKilledOtherEntity(ServerWorld world, Entity entity, LivingEntity killedEntity, DamageSource damageSource);
	}

	private ServerEntityCombatEvents() {
	}
}
