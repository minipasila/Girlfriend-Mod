package com.beckytidus.girlfriendmod.event;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Handles advancement events and triggers girlfriend reactions.
 */
public class AdvancementEventHandler {
    
    /**
     * Called when a player is granted an advancement.
     * Finds nearby girlfriends and triggers their reaction.
     */
    public static void onAdvancementGranted(ServerPlayerEntity player, AdvancementEntry advancementEntry) {
        if (player == null || player.getEntityWorld().isClient()) return;
        
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        
        // Find girlfriends belonging to this player
        List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(
            GirlFriendEntity.class,
            player.getBoundingBox().expand(20), // 20 block radius
            g -> g.getOwner() == player
        );
        
        if (girlfriends.isEmpty()) return;
        
        // Get advancement details
        Advancement advancement = advancementEntry.value();
        
        // Skip hidden advancements (like recipe unlocks) - only react to visible advancements
        if (advancement.display().isEmpty()) {
            return;
        }
        
        // Get the display info (title, frame type, etc.)
        String title = getAdvancementTitle(advancementEntry);
        AdvancementFrame frame = getAdvancementFrame(advancement);
        
        // Notify each girlfriend
        for (GirlFriendEntity gf : girlfriends) {
            gf.onOwnerAdvancement(title, frame);
        }
    }
    
    /**
     * Get the display title of an advancement.
     */
    private static String getAdvancementTitle(AdvancementEntry entry) {
        Advancement advancement = entry.value();
        
        // Try to get the display title
        if (advancement.display().isPresent()) {
            AdvancementDisplay display = advancement.display().get();
            Text titleText = display.getTitle();
            if (titleText != null) {
                return titleText.getString();
            }
        }
        
        // Fallback to the advancement name field
        if (advancement.name().isPresent()) {
            return advancement.name().get().getString();
        }
        
        // Final fallback: use the advancement ID (e.g., "minecraft:story/mine_stone")
        return entry.id().toString();
    }
    
    /**
     * Get the frame type (task/challenge/goal) of an advancement.
     */
    private static AdvancementFrame getAdvancementFrame(Advancement advancement) {
        if (advancement.display().isPresent()) {
            AdvancementDisplay display = advancement.display().get();
            return display.getFrame();
        }
        return AdvancementFrame.TASK; // Default to task
    }
}