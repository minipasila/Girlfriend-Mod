package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.util.Identifier;

/**
 * Fabric-provided extensions for {@link ServerConfigurationNetworkHandler}.
 * This interface is automatically implemented via Mixin and interface injection.
 */
public interface FabricServerConfigurationNetworkHandler {
	/**
	 * Enqueues a {@link ServerPlayerConfigurationTask} task to be processed.
	 *
	 * <p>Before adding a task use {@link ServerConfigurationNetworking#canSend(ServerConfigurationNetworkHandler, Identifier)}
	 * to ensure that the client can process this task.
	 *
	 * <p>Once the client has handled the task a packet should be sent to the server.
	 * Upon receiving this packet the server should call {@link FabricServerConfigurationNetworkHandler#completeTask(ServerPlayerConfigurationTask.Key)},
	 * otherwise the client cannot join the world.
	 *
	 * @param task the task
	 */
	default void addTask(ServerPlayerConfigurationTask task) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Completes the task identified by {@code key}.
	 *
	 * @param key the task key
	 * @throws IllegalStateException if the current task is not {@code key}
	 */
	default void completeTask(ServerPlayerConfigurationTask.Key key) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
