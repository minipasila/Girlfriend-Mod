package com.beckytidus.girlfriendmod.interaction;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;

public class ItemUseHandler {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.getItem() == ItemRegistry.GIRLFRIEND_SUMMONER && !world.isClient()) {
                GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, world);
                girlfriend.setPosition(player.getX(), player.getY(), player.getZ());
                girlfriend.setOwner(player);
                world.spawnEntity(girlfriend);

                player.sendMessage(Text.literal("♥ GirlFriend has been summoned!"), false);

                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }
}
