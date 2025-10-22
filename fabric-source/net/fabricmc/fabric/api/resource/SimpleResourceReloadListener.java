package net.fabricmc.fabric.api.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

/**
 * A simplified version of the "resource reload listener" interface, hiding the
 * peculiarities of the API.
 *
 * <p>In essence, there are two stages:
 *
 * <ul><li>load: create an instance of your data object containing all loaded and
 * processed information,
 * <li>apply: apply the information from the data object to the game instance.</ul>
 *
 * <p>The load stage should be self-contained as it can run on any thread! However,
 * the apply stage is guaranteed to run on the game thread.
 *
 * <p>For a fully synchronous alternative, consider using
 * {@link SynchronousResourceReloader} in conjunction with
 * {@link IdentifiableResourceReloadListener}.
 *
 * @param <T> The data object.
 * @deprecated Use {@link net.fabricmc.fabric.api.resource.v1.reloader.SimpleResourceReloader} instead.
 */
@Deprecated
public interface SimpleResourceReloadListener<T> extends IdentifiableResourceReloadListener {
	@Override
	default CompletableFuture<Void> reload(Store store, Executor loadExecutor, Synchronizer helper, Executor applyExecutor) {
		return load(store.getResourceManager(), loadExecutor).thenCompose(helper::whenPrepared).thenCompose(
				(o) -> apply(o, store.getResourceManager(), applyExecutor)
		);
	}

	/**
	 * Asynchronously process and load resource-based data. The code
	 * must be thread-safe and not modify game state!
	 *
	 * @param manager  The resource manager used during reloading.
	 * @param executor The executor which should be used for this stage.
	 * @return A CompletableFuture representing the "data loading" stage.
	 */
	CompletableFuture<T> load(ResourceManager manager, Executor executor);

	/**
	 * Synchronously apply loaded data to the game state.
	 *
	 * @param manager  The resource manager used during reloading.
	 * @param executor The executor which should be used for this stage.
	 * @return A CompletableFuture representing the "data applying" stage.
	 */
	CompletableFuture<Void> apply(T data, ResourceManager manager, Executor executor);
}
