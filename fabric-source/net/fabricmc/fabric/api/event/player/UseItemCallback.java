package net.fabricmc.fabric.api.event.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Callback for right-clicking ("using") an item.
 * Is hooked in before the spectator check, so make sure to check for the player's game mode as well!
 *
 * <p>Upon return:
 * <ul><li>SUCCESS cancels further processing and, on the client, sends a packet to the server.
 * <li>PASS falls back to further processing.
 * <li>FAIL cancels further processing and does not send a packet to the server.</ul>
 */
public interface UseItemCallback {
	Event<UseItemCallback> EVENT = EventFactory.createArrayBacked(UseItemCallback.class,
			listeners -> (player, world, hand) -> {
				for (UseItemCallback event : listeners) {
					ActionResult result = event.interact(player, world, hand);

					if (result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			}
	);

	ActionResult interact(PlayerEntity player, World world, Hand hand);
}
