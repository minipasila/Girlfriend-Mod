package net.fabricmc.fabric.impl.resource.v1;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

public interface FabricResourceReloader extends ResourceReloader {
	/**
	 * {@return the unique identifier of this Vanilla resource reloader}
	 */
	Identifier fabric$getId();
}
