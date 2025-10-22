package net.fabricmc.fabric.impl.event.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.BlockAttackInteractionAware;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class InteractionEventsRouter implements ModInitializer {
	@Override
	public void onInitialize() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			BlockState state = world.getBlockState(pos);

			if (state instanceof BlockAttackInteractionAware) {
				if (((BlockAttackInteractionAware) state).onAttackInteraction(state, world, pos, player, hand, direction)) {
					return ActionResult.FAIL;
				}
			} else if (state.getBlock() instanceof BlockAttackInteractionAware) {
				if (((BlockAttackInteractionAware) state.getBlock()).onAttackInteraction(state, world, pos, player, hand, direction)) {
					return ActionResult.FAIL;
				}
			}

			return ActionResult.PASS;
		});

		/*
		* This code is for telling the client that the block wasn't actually broken.
		* This covers a 3x3 area due to how vanilla redstone handles updates, as it considers
		* important functions like quasi-connectivity and redstone dust logic
		 */
		PlayerBlockBreakEvents.CANCELED.register(((world, player, pos, state, blockEntity) -> {
			BlockPos cornerPos = pos.add(-1, -1, -1);

			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					for (int z = 0; z < 3; z++) {
						((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(world, cornerPos.add(x, y, z)));
					}
				}
			}
		}));
	}
}
