package net.fabricmc.fabric.impl.transfer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DebugMessages {
	public static String forGlobalPos(@Nullable World world, BlockPos pos) {
		String dimension = world != null ? world.getDimensionEntry().getIdAsString() : "<no dimension>";
		return dimension + "@" + pos.toShortString();
	}

	public static String forPlayer(PlayerEntity player) {
		return player.getDisplayName() + "/" + player.getUuidAsString();
	}

	public static String forInventory(@Nullable Inventory inventory) {
		if (inventory == null) {
			return "~~NULL~~"; // like in crash reports
		} else if (inventory instanceof PlayerInventory playerInventory) {
			return forPlayer(playerInventory.player);
		} else {
			String result = inventory.toString();

			if (inventory instanceof BlockEntity blockEntity) {
				result += " (%s, %s)".formatted(blockEntity.getCachedState(), forGlobalPos(blockEntity.getWorld(), blockEntity.getPos()));
			}

			return result;
		}
	}
}
