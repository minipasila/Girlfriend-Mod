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
        ItemStack stack = player.getStackInHand(hand);

        // Sneak + Empty Hand = Toggle Follow/Wait
        if (stack.isEmpty()) {
            if (player.isSneaking()) {
                girlfriend.toggle();
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        // We need a copy of the stack for the AI prompt because decrementing modifies it
        ItemStack stackCopy = stack.copy();
        boolean interactionSuccess = false;

        // Check for Food or Gifts
        if (isFood(stack)) {
            girlfriend.feedEntity(stack);
            interactionSuccess = true;
        } else if (isGift(stack)) {
            girlfriend.addRelationship(3); // Bonus for non-food gifts
            interactionSuccess = true;
        }

        if (interactionSuccess) {
            // Trigger the AI reaction (speech + memory)
            girlfriend.reactToItem(player, stackCopy);
            
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        // Name Tag Handling
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
    
    private static boolean isGift(ItemStack stack) {
        // Expanded list of giftable items that are not food
        return stack.isOf(Items.DIAMOND) || stack.isOf(Items.EMERALD) || 
               stack.isOf(Items.AMETHYST_SHARD) || stack.isOf(Items.GOLD_INGOT) || 
               stack.isOf(Items.NETHERITE_INGOT) || stack.isOf(Items.POPPY) ||
               stack.isOf(Items.DANDELION) || stack.isOf(Items.BLUE_ORCHID) ||
               stack.isOf(Items.ALLIUM) || stack.isOf(Items.AZURE_BLUET) ||
               stack.isOf(Items.RED_TULIP) || stack.isOf(Items.ORANGE_TULIP) ||
               stack.isOf(Items.WHITE_TULIP) || stack.isOf(Items.PINK_TULIP) ||
               stack.isOf(Items.OXEYE_DAISY) || stack.isOf(Items.CORNFLOWER) ||
               stack.isOf(Items.LILY_OF_THE_VALLEY) || stack.isOf(Items.WITHER_ROSE) ||
               stack.isOf(Items.SUNFLOWER) || stack.isOf(Items.LILAC) ||
               stack.isOf(Items.ROSE_BUSH) || stack.isOf(Items.PEONY);
    }
}