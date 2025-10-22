package net.fabricmc.fabric.api.resource;

import net.minecraft.resource.SynchronousResourceReloader;

/**
 * A simplified version of the "resource reload listener" interface, hiding the
 * peculiarities of the API and ensuring all data is loaded on the main thread.
 *
 * @deprecated Use {@link SynchronousResourceReloader} directly.
 */
@Deprecated
public interface SimpleSynchronousResourceReloadListener extends IdentifiableResourceReloadListener, SynchronousResourceReloader {
}
