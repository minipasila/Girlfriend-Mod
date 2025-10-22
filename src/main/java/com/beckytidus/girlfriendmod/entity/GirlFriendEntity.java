package com.beckytidus.girlfriendmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class GirlFriendEntity extends PathAwareEntity {
    private int relationshipLevel = 0;
    private int maxRelationshipLevel = 100;
    private long lastPhraseTime = 0;
    private long lastGiftTime = 0;
    private long lastHealTime = 0;
    private String playerCustomName = "";
    private PlayerEntity owner;
    private boolean isFollowing = true;

    public GirlFriendEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.literal("GirlFriend"));
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

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }

    // Custom goal to follow the owner
    private class FollowOwnerGoal extends Goal {
        private static final double MIN_DISTANCE = 3.0;
        private static final double MAX_DISTANCE = 10.0;
        private static final double TELEPORT_DISTANCE = 20.0;

        @Override
        public boolean canStart() {
            return owner != null && isFollowing && !owner.isSpectator();
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            if (owner == null) return;

            double distance = GirlFriendEntity.this.distanceTo(owner);

            // Teleport if too far
            if (distance > TELEPORT_DISTANCE) {
            GirlFriendEntity.this.requestTeleport(owner.getX(), owner.getY(), owner.getZ());
                return;
            }

            // Follow if outside min distance but within max distance
            if (distance > MIN_DISTANCE && distance < MAX_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.0);
            } else if (distance >= MAX_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.2);
            } else {
                GirlFriendEntity.this.getNavigation().stop();
            }

            // Look at owner
            GirlFriendEntity.this.getLookControl().lookAt(owner, 10.0F, GirlFriendEntity.this.getMaxHeadRotation());
        }
    }

    // Custom goal to defend the owner
    private class DefendOwnerGoal extends Goal {
        private static final double DEFEND_RANGE = 15.0;

        @Override
        public boolean canStart() {
            return owner != null && !owner.isSpectator() && 
                   GirlFriendEntity.this.getTarget() == null &&
                   GirlFriendEntity.this.distanceTo(owner) <= DEFEND_RANGE;
        }

        @Override
        public boolean shouldContinue() {
            return canStart() && GirlFriendEntity.this.getTarget() != null;
        }

        @Override
        public void tick() {
            if (owner == null) return;

            // Find nearby hostile entities targeting the owner
            for (net.minecraft.entity.LivingEntity entity : GirlFriendEntity.this.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.LivingEntity.class, 
                    GirlFriendEntity.this.getBoundingBox().expand(DEFEND_RANGE), 
                    e -> e instanceof HostileEntity && ((HostileEntity) e).getTarget() == owner)) {
                
                GirlFriendEntity.this.setTarget(entity);
                if (owner != null) {
                    owner.sendMessage(Text.literal("♥ GirlFriend: I'll protect you!"), false);
                }
                break;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.owner != null) {
            long currentTime = System.currentTimeMillis();

            int phraseFrequency = Math.max(15000, 25000 - (relationshipLevel * 100));
            if (currentTime - lastPhraseTime > phraseFrequency && Math.random() < (0.1 + relationshipLevel * 0.0015)) {
                this.sayRandomPhrase();
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
        String displayName = "GirlFriend";
        if (!this.playerCustomName.isEmpty()) {
            displayName = this.playerCustomName;
        }
        displayName += " [Lv:" + this.relationshipLevel + "] [HP:" + (int)this.getHealth() + "/" + (int)this.getMaxHealth() + "]";
        this.setCustomName(Text.literal(displayName));
    }

    private void sayRandomPhrase() {
        String[] phrases = {
            "I love you so much!", "You're amazing!", "I'm always here for you",
            "You make me so happy", "Thank you for everything", "Let's fight together!",
            "I've got your back", "You're my everything", "This time with you is wonderful",
            "I'll never leave you", "Your smile brightens my day", "I'm so proud of you",
            "Let me help you", "We make a great team!", "I missed you!",
            "You're my hero!", "I trust you completely", "Let's adventure together!",
            "You make my heart skip a beat", "I think about you all the time",
            "I'd do anything for you", "You're the best!", "I'm lucky to have you",
            "You're so strong and brave", "I admire your determination",
            "Every moment with you is precious", "You brighten my world",
            "I love your laugh", "You inspire me daily", "Together we're unstoppable!"
        };

        if (this.owner != null) {
            String phrase = phrases[(int)(Math.random() * phrases.length)];
            this.owner.sendMessage(Text.literal("♥ GirlFriend: " + phrase), false);
            this.addRelationship(2);
        }
    }

    private void giveRandomGift() {
        if (this.owner != null) {
            String[] gifts = {
                "gives you a gift!", "found something for you!",
                "made something special for you!", "wants you to have this!",
                "made this just for you!", "thought of you!",
                "picked this out for you!", "brought you something!"
            };
            String giftPhrase = gifts[(int)(Math.random() * gifts.length)];
            this.owner.sendMessage(Text.literal("♥ GirlFriend " + giftPhrase), false);
            this.addRelationship(3);

            ItemStack gift = getRandomGiftItem();
            if (!this.owner.getInventory().insertStack(gift)) {
                this.owner.dropItem(gift, false);
            }
        }
    }

    private ItemStack getRandomGiftItem() {
        ItemStack[] possibleGifts = {
            new ItemStack(Items.DIAMOND),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.APPLE),
            new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.AMETHYST_SHARD),
            new ItemStack(Items.POPPY)
        };
        return possibleGifts[(int)(Math.random() * possibleGifts.length)].copy();
    }

    public PlayerEntity getOwner() {
        return this.owner;
    }

    public void setOwner(PlayerEntity player) {
        this.owner = player;
    }

    public void feedEntity(ItemStack stack) {
        if (stack.isOf(Items.APPLE) || stack.isOf(Items.GOLDEN_APPLE)) {
            this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
            this.addRelationship(5);
            if (this.owner != null) {
                this.owner.sendMessage(Text.literal("♥ GirlFriend: Thank you for the food!"), false);
            }
        } else if (stack.isOf(Items.WHEAT) || stack.isOf(Items.BREAD)) {
            this.setHealth(Math.min(this.getHealth() + 2.0f, this.getMaxHealth()));
            this.addRelationship(2);
        } else if (stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) || stack.isOf(Items.BAKED_POTATO)) {
            this.setHealth(Math.min(this.getHealth() + 3.0f, this.getMaxHealth()));
            this.addRelationship(3);
        } else if (stack.isOf(Items.PUMPKIN_PIE) || stack.isOf(Items.CAKE)) {
            this.setHealth(Math.min(this.getHealth() + 6.0f, this.getMaxHealth()));
            this.addRelationship(4);
            if (this.owner != null) {
                this.owner.sendMessage(Text.literal("♥ GirlFriend: Mmm, delicious!"), false);
            }
        }
    }

    public int getRelationshipLevel() {
        return this.relationshipLevel;
    }

    public void setRelationshipLevel(int level) {
        this.relationshipLevel = Math.min(Math.max(0, level), this.maxRelationshipLevel);
    }

    public void addRelationship(int amount) {
        this.setRelationshipLevel(this.relationshipLevel + amount);
    }

    public void setPlayerCustomName(String name) {
        this.playerCustomName = name;
    }

    public String getPlayerCustomName() {
        return this.playerCustomName;
    }

    public boolean canSleepWithOwner() {
        return this.relationshipLevel >= 50;
    }

    public void toggle() {
        this.isFollowing = !this.isFollowing;
        if (this.owner != null) {
            if (this.isFollowing) {
                this.owner.sendMessage(Text.literal("♥ GirlFriend: I'm following you!"), false);
            } else {
                this.owner.sendMessage(Text.literal("♥ GirlFriend: I'll wait here for you."), false);
            }
        }
    }

    public boolean isFollowingOwner() {
        return this.isFollowing;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.owner != null && source.getAttacker() instanceof PlayerEntity && source.getAttacker() != this.owner) {
            this.owner.sendMessage(Text.literal("♥ GirlFriend: Ouch! Why are you hurting me?"), false);
            this.addRelationship(-5);
        } else if (source.getAttacker() != null && this.owner != null) {
            // Someone else attacked us - defend ourselves!
            this.setTarget((net.minecraft.entity.LivingEntity) source.getAttacker());
            this.owner.sendMessage(Text.literal("♥ GirlFriend: I'll fight back!"), false);
        }

        boolean result = super.damage(world, source, amount);
        
        if (this.getHealth() <= 0 && this.owner != null) {
            this.owner.sendMessage(Text.literal("No! GirlFriend has fallen..."), false);
        }

        return result;
    }
}