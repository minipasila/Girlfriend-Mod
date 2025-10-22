package net.fabricmc.fabric.api.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

/**
 * Convenience interface for blocks which listen to "break interactions" (left-click).
 *
 * @deprecated Use {@link AttackBlockCallback} instead and check for the block.
 * This gives more control over the different cancellation outcomes.
 */
@Deprecated
public interface BlockAttackInteractionAware {
	/**
	 * @return True if the block accepted the player and it should no longer be processed.
	 */
	boolean onAttackInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, Direction direction);
}
