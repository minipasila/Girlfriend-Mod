package net.fabricmc.fabric.api.screenhandler.v1;

import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * An extension of {@code NamedScreenHandlerFactory} that can write additional data to a screen opening packet.
 * This is used for {@linkplain ExtendedScreenHandlerType extended screen handlers}.
 *
 * @see ExtendedScreenHandlerType usage examples
 */
public interface ExtendedScreenHandlerFactory<D> extends NamedScreenHandlerFactory {
	/**
	 * Writes additional server -&gt; client screen opening data to the buffer.
	 *
	 * @param player the player that is opening the screen
	 * @return the screen opening data
	 */
	D getScreenOpeningData(ServerPlayerEntity player);
}
