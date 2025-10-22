package com.beckytidus.girlfriendmod.interaction;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;

public class EntityInteractionHandler {
    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof GirlFriendEntity) {
                GirlFriendEntity girlfriend = (GirlFriendEntity) entity;
                return handleGirlFriendInteraction(player, girlfriend, hand);
            }
            return ActionResult.PASS;
        });
    }

    public static ActionResult handleGirlFriendInteraction(PlayerEntity player, GirlFriendEntity girlfriend, Hand hand) {
        if (player.getStackInHand(hand).isEmpty()) {
            if (player.isSneaking()) {
                girlfriend.toggle();
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        var stack = player.getStackInHand(hand);
        if (isFood(stack)) {
            girlfriend.feedEntity(stack);
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        if (stack.isOf(Items.NAME_TAG)) {
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            girlfriend.setPlayerCustomName("Custom Name");
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private static boolean isFood(ItemStack stack) {
        return stack.isOf(Items.APPLE) || stack.isOf(Items.GOLDEN_APPLE) ||
               stack.isOf(Items.WHEAT) || stack.isOf(Items.BREAD) ||
               stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) ||
               stack.isOf(Items.BAKED_POTATO) || stack.isOf(Items.PUMPKIN_PIE) ||
               stack.isOf(Items.CAKE) || stack.isOf(Items.HONEY_BOTTLE) ||
               stack.isOf(Items.MELON_SLICE) || stack.isOf(Items.BEEF) ||
               stack.isOf(Items.COOKED_BEEF) || stack.isOf(Items.PORKCHOP) ||
               stack.isOf(Items.COOKED_PORKCHOP) || stack.isOf(Items.CHICKEN) ||
               stack.isOf(Items.COOKED_CHICKEN);
    }
}
