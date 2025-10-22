package com.beckytidus.girlfriendmod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;

public class GirlFriendSummonerItem extends Item {
    public GirlFriendSummonerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            if (EntityRegistry.GIRLFRIEND != null) {
                GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, world);
                girlfriend.setPosition(user.getX(), user.getY(), user.getZ());
                girlfriend.setOwner(user);
                world.spawnEntity(girlfriend);

                user.sendMessage(Text.literal("GirlFriend has been summoned!"), false);

                if (!user.isCreative()) {
                    user.getStackInHand(hand).decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
