package com.beckytidus.girlfriendmod.interaction;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
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
        World world = player.getEntityWorld();

        // Sneak + Empty Hand = Toggle Follow/Wait
        if (stack.isEmpty()) {
            if (player.isSneaking()) {
                girlfriend.toggle();
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        // We need a copy of the stack for the AI prompt because logic might split/modify it
        ItemStack stackCopy = stack.copy();

        boolean interactionSuccess = false;
        boolean consumed = false;
        boolean storedInInventory = false;

        // Check for Food (Direct Consumption, not stored)
        if (isFood(stack)) {
            girlfriend.feedEntity(stack);
            interactionSuccess = true;
            consumed = true;
        }
        // Check for Gifts (Storage)
        else if (isGift(stack)) {
            // FIX: Perform inventory logic ONLY on the server to prevent client desync.
            // On client, we just return SUCCESS to trigger the arm swing animation, but we DO NOT decrement the stack.
            if (world.isClient()) {
                return ActionResult.SUCCESS;
            }

            // Server-Side Logic
            ItemStack toAdd = stack.copy();
            toAdd.setCount(1);

            ItemStack remainder = girlfriend.getInventory().addStack(toAdd);

            if (remainder.isEmpty()) {
                // Item accepted
                girlfriend.addRelationship(3);

                // Decrement manually on server
                if (!player.isCreative()) {
                    stack.decrement(1);
                }

                // Get the girlfriend's name for the event
                String name = girlfriend.getCustomName().getString();
                if (name == null || name.isEmpty() || name.equals("Girlfriend")) {
                    name = "Girlfriend";
                }
                String itemName = stackCopy.getName().getString();

                girlfriend.reactToItem(player, stackCopy, true);
                return ActionResult.SUCCESS;
            } else {
                // Inventory full
                player.sendMessage(net.minecraft.text.Text.literal("Her inventory is full!"), true);

                // Get the girlfriend's name for the event
                String name = girlfriend.getCustomName().getString();
                if (name == null || name.isEmpty() || name.equals("Girlfriend")) {
                    name = "Girlfriend";
                }
                String itemName = stackCopy.getName().getString();

                girlfriend.reactToItem(player, stackCopy, false);
                return ActionResult.SUCCESS;
            }
        }

        if (interactionSuccess) {
            // Trigger the AI reaction (speech + memory) for Food interactions
            girlfriend.reactToItem(player, stackCopy, storedInInventory);

            if (!player.isCreative() && consumed) {
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
        // Keep original food logic for healing
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
        // Broadened definition: if it's not food and not a name tag, treat it as a potential gift
        return !isFood(stack) && !stack.isOf(Items.NAME_TAG);
    }
}
