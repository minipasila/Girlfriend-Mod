package net.fabricmc.fabric.api.object.builder.v1.world.poi;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

/**
 * This class provides utilities to create a {@link PointOfInterestType}.
 *
 * <p>A point of interest is typically used by villagers to specify their workstation blocks, meeting zones and homes.
 * Points of interest are also used by bees to specify where their bee hive is and nether portals to find existing portals.
 */
public final class PointOfInterestHelper {
	private PointOfInterestHelper() {
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id The id of this {@link PointOfInterestType}.
	 * @param ticketCount the amount of tickets.
	 * @param searchDistance the search distance.
	 * @param blocks all the blocks where a {@link PointOfInterest} of this type will be present.
	 * @return a new {@link PointOfInterestType}.
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, int searchDistance, Block... blocks) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		for (Block block : blocks) {
			builder.addAll(block.getStateManager().getStates());
		}

		return register(id, ticketCount, searchDistance, builder.build());
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}.
	 * @param ticketCount the amount of tickets.
	 * @param searchDistance the search distance.
	 * @param blocks all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link PointOfInterestType}.
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, int searchDistance, Iterable<BlockState> blocks) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		return register(id, ticketCount, searchDistance, builder.addAll(blocks).build());
	}

	// INTERNAL METHODS

	private static PointOfInterestType register(Identifier id, int ticketCount, int searchDistance, Set<BlockState> states) {
		return PointOfInterestTypes.register(Registries.POINT_OF_INTEREST_TYPE, RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, id), states, ticketCount, searchDistance);
	}
}
