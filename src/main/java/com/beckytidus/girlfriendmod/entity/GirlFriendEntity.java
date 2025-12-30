package com.beckytidus.girlfriendmod.entity;

import com.beckytidus.girlfriendmod.ai.ChutesClient;
import com.beckytidus.girlfriendmod.ai.ConversationManager;
import com.beckytidus.girlfriendmod.config.ModConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.UUID;

public class GirlFriendEntity extends PathAwareEntity {
    private int relationshipLevel = 0;
    private int maxRelationshipLevel = 100;
    private long lastPhraseTime = 0;
    private long lastGiftTime = 0;
    private long lastHealTime = 0;
    private String playerCustomName = "";
    
    private PlayerEntity owner;
    private UUID ownerUuid; 
    
    private boolean isFollowing = true;
    
    // AI Components
    private ConversationManager conversationManager;
    private String gameContext = "Standing idly.";

    public GirlFriendEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.literal("Girlfriend"));
    }
    
    // --- Persistence Logic (NBT / Data Views) ---
    // Updated for 1.21.6+: Uses WriteView instead of NbtCompound
    @Override
    public void writeCustomData(WriteView nbt) {
        super.writeCustomData(nbt);
        nbt.putInt("RelationshipLevel", this.relationshipLevel);
        nbt.putString("CustomName", this.playerCustomName);
        nbt.putBoolean("IsFollowing", this.isFollowing);
        
        // Manual UUID storage
        if (this.ownerUuid != null) {
            nbt.putLong("OwnerMost", this.ownerUuid.getMostSignificantBits());
            nbt.putLong("OwnerLeast", this.ownerUuid.getLeastSignificantBits());
        }
    }

    // Updated for 1.21.6+: Uses ReadView instead of NbtCompound
    @Override
    public void readCustomData(ReadView nbt) {
        super.readCustomData(nbt);
        
        // Uses default values to handle Optional returns
        this.relationshipLevel = nbt.getInt("RelationshipLevel", 0);
        this.playerCustomName = nbt.getString("CustomName", "");
        this.isFollowing = nbt.getBoolean("IsFollowing", true);
        
        // Manual UUID retrieval
        long most = nbt.getLong("OwnerMost", 0L);
        long least = nbt.getLong("OwnerLeast", 0L);
        if (most != 0L && least != 0L) {
            this.ownerUuid = new UUID(most, least);
        }
    }
    // -------------------------------

    private ConversationManager getMemory() {
        if (conversationManager == null) {
            conversationManager = new ConversationManager(this.getUuid());
        }
        return conversationManager;
    }

    public static DefaultAttributeContainer.Builder createGirlfriendAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(3, new LookAroundGoal(this));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(5, new FollowOwnerGoal());
        this.goalSelector.add(6, new DefendOwnerGoal());
        
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, net.minecraft.entity.mob.HostileEntity.class, true));
    }

    private class FollowOwnerGoal extends Goal {
        private static final double MIN_DISTANCE = 3.0;
        private static final double MAX_DISTANCE = 10.0;
        private static final double TELEPORT_DISTANCE = 20.0;
        @Override
        public boolean canStart() { return owner != null && isFollowing && !owner.isSpectator(); }
        @Override
        public boolean shouldContinue() { return canStart(); }
        @Override
        public void tick() {
            if (owner == null) return;
            double distance = GirlFriendEntity.this.distanceTo(owner);
            if (distance > TELEPORT_DISTANCE) {
                GirlFriendEntity.this.requestTeleport(owner.getX(), owner.getY(), owner.getZ());
                return;
            }
            if (distance > MIN_DISTANCE && distance < MAX_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.0);
            } else if (distance >= MAX_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.2);
            } else {
                GirlFriendEntity.this.getNavigation().stop();
            }
            GirlFriendEntity.this.getLookControl().lookAt(owner, 10.0F, GirlFriendEntity.this.getMaxHeadRotation());
        }
    }

    private class DefendOwnerGoal extends Goal {
        private static final double DEFEND_RANGE = 15.0;
        @Override
        public boolean canStart() {
            return owner != null && !owner.isSpectator() && 
                   GirlFriendEntity.this.getTarget() == null &&
                   GirlFriendEntity.this.distanceTo(owner) <= DEFEND_RANGE;
        }
        @Override
        public boolean shouldContinue() { return canStart() && GirlFriendEntity.this.getTarget() != null; }
        @Override
        public void tick() {
            if (owner == null) return;
            for (net.minecraft.entity.LivingEntity entity : GirlFriendEntity.this.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.LivingEntity.class, 
                    GirlFriendEntity.this.getBoundingBox().expand(DEFEND_RANGE), 
                    e -> e instanceof HostileEntity && ((HostileEntity) e).getTarget() == owner)) {
                
                GirlFriendEntity.this.setTarget(entity);
                updateGameContext("Protecting player from " + entity.getName().getString());
                break;
            }
        }
    }

    public void updateGameContext(String ctx) {
        this.gameContext = ctx;
    }
    
    public void processPlayerChat(String msg) {
        if (!ModConfig.get().enableAI) return;
        
        getMemory().addMessage("user", msg);
        
        ChutesClient.generateResponse(getMemory().getContextWindow(), 
            "Current action: " + gameContext + ". Relationship Lv: " + relationshipLevel)
            .thenAccept(response -> {
                getMemory().addMessage("assistant", response);
                if (this.owner != null) {
                    this.owner.sendMessage(Text.literal("<" + this.getName().getString() + "> " + response).formatted(Formatting.LIGHT_PURPLE), false);
                }
            });
    }

    @Override
    public void tick() {
        super.tick();

        // Restore owner reference from UUID if needed (fixes issue after restart)
        // Fixed: Use isClient() method instead of field
        if (this.owner == null && this.ownerUuid != null && !this.getEntityWorld().isClient()) {
            this.owner = this.getEntityWorld().getPlayerByUuid(this.ownerUuid);
        }

        if (this.owner != null) {
            long currentTime = System.currentTimeMillis();
            long phraseFrequency = 120000 + (long)(Math.random() * 180000); 
            
            if (currentTime - lastPhraseTime > phraseFrequency) {
                this.sayAIComment();
                lastPhraseTime = currentTime;
            }

            int giftFrequency = Math.max(60000, 90000 - (relationshipLevel * 200));
            if (currentTime - lastGiftTime > giftFrequency && Math.random() < (0.06 + relationshipLevel * 0.0012)) {
                this.giveRandomGift();
                lastGiftTime = currentTime;
            }

            if (this.getHealth() < this.getMaxHealth() * 0.7 && currentTime - lastHealTime > 40000) {
                this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
                lastHealTime = currentTime;
            }

            this.updateNameTag();
        }
    }
    
    private void updateNameTag() {
        String displayName = "Girlfriend";
        if (!this.playerCustomName.isEmpty()) displayName = this.playerCustomName;
        displayName += " [Lv:" + this.relationshipLevel + "]";
        this.setCustomName(Text.literal(displayName));
    }

    private void giveRandomGift() {
        if (this.owner != null) {
             getMemory().addMessage("system", "You gave the player a gift.");
             ItemStack gift = getRandomGiftItem();
             if (!this.owner.getInventory().insertStack(gift)) {
                 this.owner.dropItem(gift, false);
             }
             this.addRelationship(3);
        }
    }
    
    private ItemStack getRandomGiftItem() {
        ItemStack[] possibleGifts = {
            new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD),
            new ItemStack(Items.APPLE), new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.AMETHYST_SHARD), new ItemStack(Items.POPPY)
        };
        return possibleGifts[(int)(Math.random() * possibleGifts.length)].copy();
    }
    
    public PlayerEntity getOwner() { return this.owner; }
    
    public void setOwner(PlayerEntity player) { 
        this.owner = player;
        this.ownerUuid = player.getUuid(); // Save UUID for persistence
    }

    private void sayAIComment() {
        if (!ModConfig.get().enableAI || this.owner == null) return;
        
        String prompt = "Spontaneously comment on the current situation or show affection.";
        if (this.getEntityWorld().isNight()) prompt += " It is night time.";
        if (this.getHealth() < 10) prompt += " You are hurt.";
        
        ChutesClient.generateResponse(getMemory().getContextWindow(), 
            prompt + " Context: " + gameContext)
            .thenAccept(response -> {
                getMemory().addMessage("assistant", response);
                this.owner.sendMessage(Text.literal("<" + this.getName().getString() + "> " + response).formatted(Formatting.LIGHT_PURPLE), false);
            });
    }

    public void feedEntity(ItemStack stack) {
        if (stack.isOf(Items.APPLE) || stack.isOf(Items.GOLDEN_APPLE)) {
            this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
            this.addRelationship(5);
            getMemory().addMessage("system", "Player fed you an Apple. You loved it.");
        } else if (stack.isOf(Items.WHEAT) || stack.isOf(Items.BREAD)) {
            this.setHealth(Math.min(this.getHealth() + 2.0f, this.getMaxHealth()));
            this.addRelationship(2);
        } else if (stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BAKED_POTATO)) {
            this.setHealth(Math.min(this.getHealth() + 3.0f, this.getMaxHealth()));
            this.addRelationship(3);
        } else if (stack.isOf(Items.PUMPKIN_PIE) || stack.isOf(Items.CAKE)) {
            this.setHealth(Math.min(this.getHealth() + 6.0f, this.getMaxHealth()));
            this.addRelationship(4);
            getMemory().addMessage("system", "Player gave you cake/pie. It was delicious.");
        }
    }

    public void clearMemory() {
        getMemory().clear();
        if (owner != null) owner.sendMessage(Text.literal("Girlfriend memory cleared."), true);
    }

    public void setRelationshipLevel(int level) {
        this.relationshipLevel = Math.min(Math.max(0, level), this.maxRelationshipLevel);
    }
    public void addRelationship(int amount) {
        this.setRelationshipLevel(this.relationshipLevel + amount);
    }
    public void setPlayerCustomName(String name) { this.playerCustomName = name; }
    public String getPlayerCustomName() { return this.playerCustomName; }
    public void toggle() {
        this.isFollowing = !this.isFollowing;
        if (this.owner != null) {
            String status = this.isFollowing ? "following you" : "waiting here";
            getMemory().addMessage("system", "You are now " + status);
            this.owner.sendMessage(Text.literal("♥ " + this.getName().getString() + ": I'm " + status), false);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.getAttacker() != null) {
            updateGameContext("Attacked by " + source.getAttacker().getName().getString());
            getMemory().addMessage("system", "You were attacked by " + source.getAttacker().getName().getString());
        }
        
        if (this.owner != null && source.getAttacker() instanceof PlayerEntity && source.getAttacker() != this.owner) {
            this.addRelationship(-5);
        }
        
        return super.damage(world, source, amount);
    }
}