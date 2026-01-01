package com.beckytidus.girlfriendmod.entity;

import com.beckytidus.girlfriendmod.ai.AIClientManager;
import com.beckytidus.girlfriendmod.ai.ChutesClient;
import com.beckytidus.girlfriendmod.ai.ConversationManager;
import com.beckytidus.girlfriendmod.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GirlFriendEntity extends PathAwareEntity implements InventoryOwner, RangedAttackMob {
    private int relationshipLevel = 0;
    private int maxRelationshipLevel = 100;
    
    // Timer for spontaneous phrases
    private long lastPhraseTime = 0;
    private long lastHealTime = 0;
    private long lastInventoryCheckTime = 0;
    
    // Cooldown for damage reactions (both self and player)
    private long lastDamageReactionTime = 0;
    private static final long DAMAGE_REACTION_COOLDOWN = 10000; // 10 seconds
    private static final long INVENTORY_CHECK_COOLDOWN = 30000; // 30 seconds

    // Kill reaction logic
    private long lastKillReactionTime = 0;
    private static final long KILL_REACTION_COOLDOWN = 20000; // 20 seconds
    private LivingEntity monitoredTarget;

    // Spam prevention for AI generation
    private boolean isGeneratingResponse = false;
    private static final long SPEECH_COOLDOWN = 3000; // Minimum 3 seconds between AI messages

    // Respawn / Knockout Logic
    private boolean isKnockedOut = false;
    private int respawnTimer = 0;
    private static final int RESPAWN_DELAY_TICKS = 400; // 20 seconds
    private boolean hasReactedToOwnerDeath = false; // Prevent spam if owner stays dead

    // Time awareness state
    private boolean isNight = false;
    private boolean firstTimeTick = true;

    private String playerCustomName = "";
    
    private PlayerEntity owner;
    private UUID ownerUuid; 
    
    private boolean isFollowing = true;
    
    // Inventory: 36 slots
    private final SimpleInventory inventory = new SimpleInventory(36);
    
    // AI Components
    private ConversationManager conversationManager;
    private String gameContext = "Standing idly.";

    // Combat State
    private MeleeAttackGoal meleeAttackGoal;
    private ProjectileAttackGoal bowAttackGoal;
    private boolean wasUsingBow = false;

    public enum GiveResult {
        SUCCESS,
        FULL,
        NOT_FOUND
    }

    public GirlFriendEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setCustomName(Text.literal("Girlfriend"));
        this.inventory.addListener(inv -> {}); 
        
        // FIX: Disable vanilla loot pickup. We handle pickup manually in pickupNearbyItems().
        this.setCanPickUpLoot(false);
    }
    
    @Override
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    // --- Persistence Logic (NBT / Data Views) ---
    @Override
    public void writeCustomData(WriteView nbt) {
        super.writeCustomData(nbt);
        nbt.putInt("RelationshipLevel", this.relationshipLevel);
        nbt.putString("CustomName", this.playerCustomName);
        nbt.putBoolean("IsFollowing", this.isFollowing);
        
        nbt.putBoolean("IsKnockedOut", this.isKnockedOut);
        nbt.putInt("RespawnTimer", this.respawnTimer);
        
        nbt.putBoolean("IsNight", this.isNight);
        
        if (this.ownerUuid != null) {
            nbt.putLong("OwnerMost", this.ownerUuid.getMostSignificantBits());
            nbt.putLong("OwnerLeast", this.ownerUuid.getLeastSignificantBits());
        }

        this.writeInventory(nbt);
    }

    @Override
    public void readCustomData(ReadView nbt) {
        super.readCustomData(nbt);

        // FIX: Force CanPickUpLoot to false even if true in saved NBT data to fix existing entities
        this.setCanPickUpLoot(false);
        
        this.relationshipLevel = nbt.getInt("RelationshipLevel", 0);
        this.playerCustomName = nbt.getString("CustomName", "");
        this.isFollowing = nbt.getBoolean("IsFollowing", true);
        
        this.isKnockedOut = nbt.getBoolean("IsKnockedOut", false);
        this.respawnTimer = nbt.getInt("RespawnTimer", 0);
        
        this.isNight = nbt.getBoolean("IsNight", false);
        this.firstTimeTick = false; 
        
        long most = nbt.getLong("OwnerMost", 0L);
        long least = nbt.getLong("OwnerLeast", 0L);
        if (most != 0L && least != 0L) {
            this.ownerUuid = new UUID(most, least);
        }

        this.readInventory(nbt);
    }

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
                .add(EntityAttributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void initGoals() {
        this.meleeAttackGoal = new MeleeAttackGoal(this, 1.2, false);
        this.bowAttackGoal = new ProjectileAttackGoal(this, 1.0, 20, 15.0f);

        this.goalSelector.add(0, new SwimGoal(this));
        
        this.goalSelector.add(2, meleeAttackGoal); 
        
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new FollowOwnerGoal());
        
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new OwnerSupportGoal()); 
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, HostileEntity.class, true)); 
    }

    // --- Combat Logic ---
    
    private void tickCombatLogic() {
        if (this.isKnockedOut) return;  // Add this line
        if (this.age % 20 != 0) return;

        ItemStack bestWeapon = ItemStack.EMPTY;
        boolean hasSword = false;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isIn(ItemTags.SWORDS)) { 
                bestWeapon = stack;
                hasSword = true;
                break;
            }
        }

        if (!hasSword) {
            ItemStack bow = ItemStack.EMPTY;
            boolean hasArrows = false;

            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (stack.getItem() instanceof BowItem) {
                    bow = stack;
                }
                if (stack.getItem() instanceof ArrowItem) {
                    hasArrows = true;
                }
            }

            if (bow != ItemStack.EMPTY && hasArrows) {
                bestWeapon = bow;
            }
        }

        ItemStack current = this.getMainHandStack();
        
        if (current != bestWeapon) {
            if (bestWeapon.isEmpty() && !current.isEmpty()) {
                boolean isCurrentItemInInventory = false;
                for(int i=0; i<inventory.size(); i++) {
                    if (inventory.getStack(i) == current) { 
                        isCurrentItemInInventory = true;
                        break;
                    }
                }
                
                if (!isCurrentItemInInventory) {
                    this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                }
            } else {
                this.equipStack(EquipmentSlot.MAINHAND, bestWeapon);
            }
        }

        boolean isUsingBow = bestWeapon.getItem() instanceof BowItem;
        
        if (isUsingBow != this.wasUsingBow) {
            if (isUsingBow) {
                this.goalSelector.remove(meleeAttackGoal);
                this.goalSelector.add(2, bowAttackGoal);
            } else {
                this.goalSelector.remove(bowAttackGoal);
                this.goalSelector.add(2, meleeAttackGoal);
            }
            this.wasUsingBow = isUsingBow;
        }
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack arrowStack = ItemStack.EMPTY;
        int arrowSlot = -1;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof ArrowItem) {
                arrowStack = stack;
                arrowSlot = i;
                break;
            }
        }

        if (arrowStack.isEmpty()) return;

        ItemStack weaponStack = this.getMainHandStack();
        PersistentProjectileEntity projectile = ProjectileUtil.createArrowProjectile(this, arrowStack, pullProgress, weaponStack);

        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - projectile.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        projectile.setVelocity(d, e + g * 0.20000000298023224, f, 1.6F, (float)(14 - this.getEntityWorld().getDifficulty().getId() * 4));

        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getEntityWorld().spawnEntity(projectile);

        arrowStack.decrement(1);
        if (arrowStack.isEmpty()) {
            inventory.setStack(arrowSlot, ItemStack.EMPTY);
        }
    }

@Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other, DamageSource source) {
        boolean result = super.onKilledOther(world, other, source);
        // Direct kill reaction
        triggerKillReaction(other);
        return result;
    }

    private void triggerKillReaction(Entity enemy) {
        if (!ModConfig.get().enableAI || isKnockedOut) return;
        
        long currentTime = System.currentTimeMillis();
        // Cooldown check for kills (prevents farm spam)
        if (currentTime - lastKillReactionTime < KILL_REACTION_COOLDOWN) return;
        
        // Don't interrupt if already speaking recently
        if (isGeneratingResponse || currentTime - lastPhraseTime < SPEECH_COOLDOWN) return;

        this.lastKillReactionTime = currentTime;
        String enemyName = enemy.getName().getString();
        
        // Contextual Reaction
        generateAndSayResponse("You (or we) have just defeated " + enemyName + ". React with relief, victory, or a short comment.");
    }

    private class OwnerSupportGoal extends Goal {
        public OwnerSupportGoal() {
            this.setControls(EnumSet.of(Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (owner == null) return false;
            return owner.getAttacking() != null || owner.getAttacker() != null;
        }

        @Override
        public void start() {
            LivingEntity target = owner.getAttacker(); 
            if (target == null) {
                target = owner.getAttacking(); 
            }

            if (target != null && target.isAlive() && !(target instanceof PlayerEntity) && !(target instanceof GirlFriendEntity)) {
                GirlFriendEntity.this.setTarget(target);
                updateGameContext("Helping owner fight " + target.getName().getString());
            }
        }
    }

    private class FollowOwnerGoal extends Goal {
        private static final double MIN_DISTANCE = 3.0;
        private static final double MAX_DISTANCE_TO_WALK = 6.0;
        private static final double TELEPORT_DISTANCE = 20.0;

        @Override
        public boolean canStart() {
            return !isKnockedOut && owner != null && isFollowing && !owner.isSpectator();
        }

        @Override
        public boolean shouldContinue() {
            return canStart() && !GirlFriendEntity.this.getNavigation().isIdle();
        }

        @Override
        public void stop() {
            GirlFriendEntity.this.getNavigation().stop();
            GirlFriendEntity.this.setSprinting(false);
            super.stop();
        }

        @Override
        public void tick() {
            if (owner == null) return;
            if (GirlFriendEntity.this.getTarget() != null && GirlFriendEntity.this.distanceTo(owner) < 15.0) {
                return;
            }

            double distance = GirlFriendEntity.this.distanceTo(owner);
            GirlFriendEntity.this.getLookControl().lookAt(owner, 10.0F, GirlFriendEntity.this.getMaxHeadRotation());

            if (distance > TELEPORT_DISTANCE) {
                GirlFriendEntity.this.requestTeleport(owner.getX(), owner.getY(), owner.getZ());
                return;
            }

            boolean shouldSprint = owner.isSprinting() || distance > MAX_DISTANCE_TO_WALK;
            GirlFriendEntity.this.setSprinting(shouldSprint);

            if (distance > MIN_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.0);
            } else {
                GirlFriendEntity.this.getNavigation().stop();
                if (!owner.isSprinting()) {
                    GirlFriendEntity.this.setSprinting(false);
                }
            }
        }
    }

    public void updateGameContext(String ctx) {
        this.gameContext = ctx;
    }
    
    private String buildSystemContext(String specificActionContext) {
        StringBuilder sb = new StringBuilder();
        if (specificActionContext != null && !specificActionContext.isEmpty()) {
            sb.append(specificActionContext).append(". ");
        }
        
        long time = this.getEntityWorld().getTimeOfDay() % 24000;
        String timeDesc = "Day";
        if (time >= 23000 || time < 1000) timeDesc = "Sunrise/Morning";
        else if (time >= 12000 && time < 13000) timeDesc = "Sunset/Dusk";
        else if (time >= 13000) timeDesc = "Night";
        
        sb.append("Time: ").append(timeDesc).append(" (").append(time).append("). ");
        
        sb.append("Current Activity: ").append(this.gameContext).append(". ");
        sb.append(getInventoryContextString()).append(". ");
        sb.append("My Status - Health: ").append((int)this.getHealth()).append("/").append((int)this.getMaxHealth());
        sb.append(", Armor: ").append(this.getArmor()).append(". ");
        sb.append("Relationship: ").append(this.relationshipLevel).append("/100. ");
        
        if (this.owner != null) {
            sb.append("Owner Status - Health: ").append((int)this.owner.getHealth()).append("/").append((int)this.owner.getMaxHealth());
            sb.append(", Armor: ").append(this.owner.getArmor()).append(" (20/20 is full for normal players). ");
        }
        
        if (this.isKnockedOut) {
            sb.append("STATUS: KNOCKED OUT / UNCONSCIOUS. ");
        }
        return sb.toString();
    }
    
    // --- Death and Respawn Overrides ---

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source) || this.isKnockedOut) {
            return false;
        }

        if (this.getHealth() - amount <= 0) {
            startKnockoutProcess(source);
            return false;
        }
        
        boolean damaged = super.damage(world, source, amount);
        
        if (damaged) {
            if (source.getAttacker() != null) {
                updateGameContext("Attacked by " + source.getAttacker().getName().getString());
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDamageReactionTime > DAMAGE_REACTION_COOLDOWN) {
                    triggerDamageReaction(source.getAttacker(), true);
                }
            }
            if (this.owner != null && source.getAttacker() instanceof PlayerEntity && source.getAttacker() != this.owner) {
                this.addRelationship(-5);
            }
        }
        
        return damaged;
    }

    private void startKnockoutProcess(DamageSource source) {
        this.isKnockedOut = true;
        this.setHealth(1.0f);
        this.respawnTimer = RESPAWN_DELAY_TICKS;
        
        this.setTarget(null);
        this.getNavigation().stop();
        this.setSprinting(false);
        
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, RESPAWN_DELAY_TICKS, 0, false, false));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, RESPAWN_DELAY_TICKS, 10, false, false));
        
        String attackerName = (source.getAttacker() != null) ? source.getAttacker().getName().getString() : "something";
        generateAndSayResponse("You have been defeated by " + attackerName + " and are falling unconscious. Say a dramatic goodbye.");
        
        if (this.owner != null) {
            this.owner.sendMessage(Text.literal("⚠ " + this.getName().getString() + " has been knocked out! She will respawn in 20 seconds.").formatted(Formatting.RED), true);
        }
    }

    private void tickRespawnLogic() {
        if (!this.isKnockedOut) return;
        
        if (this.getEntityWorld() instanceof ServerWorld serverWorld && this.age % 10 == 0) {
            serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.POOF, this.getX(), this.getY() + 0.5, this.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
        }
        
        this.respawnTimer--;
        
        if (this.respawnTimer <= 0) {
            performRespawn();
        }
    }

    private void performRespawn() {
        this.isKnockedOut = false;
        this.setHealth(this.getMaxHealth());
        this.removeStatusEffect(StatusEffects.INVISIBILITY);
        this.removeStatusEffect(StatusEffects.WEAKNESS);
        
        if (this.owner != null) {
            this.requestTeleport(this.owner.getX(), this.owner.getY(), this.owner.getZ());
        }
        
        generateAndSayResponse("You have just regained consciousness and respawned near your owner. Express relief or happiness to be back.");
        
        if (this.owner != null) {
            this.owner.sendMessage(Text.literal("♥ " + this.getName().getString() + " has revived!").formatted(Formatting.GREEN), true);
        }
    }

    // --- External Owner Events ---

    public void onOwnerDied(PlayerEntity owner) {
        if (hasReactedToOwnerDeath) return;
        hasReactedToOwnerDeath = true;
        
        generateAndSayResponse("Your owner just died right in front of you! Scream or cry out in shock.");
    }

    public void onOwnerRespawned(PlayerEntity newOwnerEntity) {
        this.setOwner(newOwnerEntity);
        this.hasReactedToOwnerDeath = false;
        
        this.requestTeleport(newOwnerEntity.getX(), newOwnerEntity.getY(), newOwnerEntity.getZ());
        
        this.setHealth(this.getMaxHealth());
        
        generateAndSayResponse("Your owner has just respawned/came back to life. You are teleported to them. Express joy and relief that they are okay.");
    }

    // --- Standard Methods ---

    public void processPlayerChat(String msg) {
        if (!ModConfig.get().enableAI) return;
        if (isKnockedOut) return;
        
        getMemory().addMessage("user", msg);
        List<ChutesClient.ChatMessage> recentHistory = getMemory().getRecentHistory(6);
        AIClientManager.analyzeIntent(msg, recentHistory).thenAccept(isAskingForGift -> {
            if (!isAskingForGift) {
                generateAndSayResponse("User said: \"" + msg + "\"");
                return;
            }
            handleGiftRequest(msg, recentHistory);
        });
    }

    private void handleGiftRequest(String userMessage, List<ChutesClient.ChatMessage> recentHistory) {
        List<String> itemNames = getItemNamesFromInventory();
        if (itemNames.isEmpty()) {
            generateAndSayResponse("The user asked for a gift, but your inventory is empty.");
            return;
        }

        AIClientManager.selectItemFromInventory(itemNames, userMessage, recentHistory).thenAccept(decision -> {
            World world = this.getEntityWorld();
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.getServer().execute(() -> {
                    String resultContext;
                    if (decision.equalsIgnoreCase("MISSING")) {
                        resultContext = "The user asked for a specific item, but you do not have it.";
                    } else {
                        GiveResult result = this.giveSpecificItem(decision);
                        switch (result) {
                            case SUCCESS -> {
                                resultContext = "You successfully gave the user your " + decision + ".";
                                this.addRelationship(2); 
                            }
                            case FULL -> {
                                resultContext = "You tried to give " + decision + " but the player's inventory is full. Inform them they need to make space.";
                            }
                            default -> {
                                resultContext = "You tried to give " + decision + " but failed/could not find it.";
                            }
                        }
                    }
                    generateAndSayResponse(resultContext);
                });
            }
        });
    }

    private void generateAndSayResponse(String promptContext) {
        if (isGeneratingResponse) return;
        isGeneratingResponse = true;

        String fullContext = buildSystemContext(promptContext);

        AIClientManager.generateResponse(getMemory().getContextWindow(), fullContext)
            .thenAccept(response -> {
                isGeneratingResponse = false;
                this.lastPhraseTime = System.currentTimeMillis();
                getMemory().addMessage("assistant", response);
                if (this.owner != null) {
                    this.owner.sendMessage(Text.literal("<" + this.getName().getString() + "> " + response).formatted(Formatting.LIGHT_PURPLE), false);
                }
            }).exceptionally(e -> {
                isGeneratingResponse = false;
                return null;
            });
    }

    private String getInventoryContextString() {
        if (inventory.isEmpty()) return "Inventory: [Empty]";
        StringBuilder sb = new StringBuilder("Inventory: [");
        boolean first = true;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack s = inventory.getStack(i);
            if (!s.isEmpty()) {
                if (!first) sb.append(", ");
                sb.append(s.getCount()).append("x ").append(s.getName().getString());
                first = false;
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private List<String> getItemNamesFromInventory() {
        List<String> names = new ArrayList<>();
        for(int i=0; i<inventory.size(); i++) {
            ItemStack s = inventory.getStack(i);
            if(!s.isEmpty()) {
                names.add(s.getName().getString());
            }
        }
        return names;
    }

    public GiveResult giveSpecificItem(String targetItemName) {
        if (this.owner == null || this.inventory.isEmpty()) return GiveResult.NOT_FOUND;
        
        for(int i=0; i<inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                String stackName = stack.getName().getString();
                if (stackName.equalsIgnoreCase(targetItemName) || stackName.toLowerCase().contains(targetItemName.toLowerCase())) {
                    ItemStack toGive = stack.copy();
                    toGive.setCount(1);
                    
                    if (this.owner.getInventory().insertStack(toGive)) {
                        stack.decrement(1);
                        if (stack.isEmpty()) {
                            inventory.setStack(i, ItemStack.EMPTY);
                        }
                        return GiveResult.SUCCESS;
                    } else {
                        return GiveResult.FULL;
                    }
                }
            }
        }
        return GiveResult.NOT_FOUND;
    }
    
    public PlayerEntity getOwner() { return this.owner; }
    
    public void setOwner(PlayerEntity player) { 
        this.owner = player;
        this.ownerUuid = player.getUuid(); 
    }

    private void sayAIComment() {
        if (!ModConfig.get().enableAI || this.owner == null || isKnockedOut) return;
        if (isGeneratingResponse || System.currentTimeMillis() - lastPhraseTime < SPEECH_COOLDOWN) return;
        String prompt = "Spontaneously comment on the situation.";
        if (this.getHealth() < 10) prompt += " You are hurt.";
        generateAndSayResponse(prompt);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isKnockedOut) {
            tickRespawnLogic();
            // Clear combat state when knocked out
            this.setTarget(null);
            this.getNavigation().stop();
            this.setSprinting(false);

            if (!this.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                 this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 40, 0, false, false));
            }
            return;
        }

        if (this.owner == null && this.ownerUuid != null && !this.getEntityWorld().isClient()) {
            this.owner = this.getEntityWorld().getPlayerByUuid(this.ownerUuid);
        }

        if (!this.getEntityWorld().isClient()) {
            if (this.age % 20 == 0) {
                pickupNearbyItems();
                tickTimeAwareness();
                tickCombatLogic();
            }
            if (this.age % 40 == 0) tickAutoEat();
            if (this.age % 100 == 0) tickInventoryManagement();

            // Check target death logic (if she was targeting someone and they died)
            LivingEntity currentTarget = this.getTarget();
            if (this.monitoredTarget != null && !this.monitoredTarget.isAlive()) {
                // If the target is no longer alive, trigger kill reaction
                // This covers cases where she helped but didn't get the kill
                triggerKillReaction(this.monitoredTarget);
                this.monitoredTarget = null;
            }
            
            // Update monitored target
            if (currentTarget != null && currentTarget.isAlive()) {
                this.monitoredTarget = currentTarget;
            }
        }

        if (this.owner != null) {
            long currentTime = System.currentTimeMillis();
            
            if (this.owner.hurtTime > 0 && this.owner.getAttacker() != null) {
                if (currentTime - lastDamageReactionTime > DAMAGE_REACTION_COOLDOWN) {
                    triggerDamageReaction(this.owner.getAttacker(), false);
                }
            }

            long phraseFrequency = 120000 + (long)(Math.random() * 180000); 
            if (currentTime - lastPhraseTime > phraseFrequency) {
                this.sayAIComment();
            }

            if (this.getHealth() < this.getMaxHealth() * 0.7 && currentTime - lastHealTime > 40000 && this.getTarget() == null) {
                this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
                lastHealTime = currentTime;
            }

            this.updateNameTag();
        }
    }

    private void tickTimeAwareness() {
        if (this.getEntityWorld().isClient()) return;
        if (this.isKnockedOut) return;

        long time = this.getEntityWorld().getTimeOfDay() % 24000;
        
        boolean currentlyNight = time >= 13000 && time < 23000;
        
        if (firstTimeTick) {
            this.isNight = currentlyNight;
            this.firstTimeTick = false;
            return;
        }

        if (currentlyNight && !this.isNight) {
            this.isNight = true;
            if (!isGeneratingResponse && System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {
                 generateAndSayResponse("It has just become night time. It is getting dark. Comment on this and maybe tell the player to be careful.");
            }
        } else if (!currentlyNight && this.isNight) {
            this.isNight = false;
            if (!isGeneratingResponse && System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {
                 generateAndSayResponse("The sun is rising and it is morning. React happily to the new day.");
            }
        }
    }

    // --- Auto Eat Logic ---
    private void tickAutoEat() {
        if (this.getHealth() >= this.getMaxHealth()) return;

        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            if (stack.contains(DataComponentTypes.FOOD)) {
                 FoodComponent food = stack.get(DataComponentTypes.FOOD);
                 if (food != null) {
                     this.eatFood(stack, i, food);
                     return;
                 }
            } else if (stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            }
        }
    }

    private void eatFood(ItemStack stack, int slot, FoodComponent food) {
        this.heal(food.nutrition()); 
        this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        
        stack.decrement(1);
        if (stack.isEmpty()) {
            this.inventory.setStack(slot, ItemStack.EMPTY);
        }
        
        this.lastHealTime = System.currentTimeMillis();
    }

    // --- Inventory Management Logic ---
    private void tickInventoryManagement() {
        if (!ModConfig.get().enableAI) return;
        if (System.currentTimeMillis() - lastInventoryCheckTime < INVENTORY_CHECK_COOLDOWN) return;
        
        if (isInventoryFull()) {
            lastInventoryCheckTime = System.currentTimeMillis();
            
            List<String> items = getItemNamesFromInventory();
            getMemory().addMessage("system", "Inventory is full. Checking logic engine for useless items to drop...");
            
            AIClientManager.selectItemToDrop(items, getMemory().getRecentHistory(5))
                .thenAccept(itemToDrop -> {
                     World world = this.getEntityWorld();
                     if (world instanceof ServerWorld serverWorld) {
                         serverWorld.getServer().execute(() -> {
                             if (!itemToDrop.equals("NONE")) {
                                 this.dropItemByName(itemToDrop);
                             }
                         });
                     }
                });
        }
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) return false;
        }
        return true;
    }

    private void dropItemByName(String itemName) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getName().getString().equalsIgnoreCase(itemName)) {
                
                ItemStack toDrop = stack.split(stack.getCount());
                inventory.setStack(i, ItemStack.EMPTY);
                
                if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
                    ItemEntity itemEntity = new ItemEntity(serverWorld, this.getX(), this.getEyeY() - 0.3, this.getZ(), toDrop);
                    
                    Vec3d look = this.getRotationVec(1.0f);
                    itemEntity.setVelocity(look.x * 0.6, 0.3, look.z * 0.6);
                    itemEntity.setPickupDelay(60); 
                    
                    serverWorld.spawnEntity(itemEntity);
                }
                
                String context = "Your inventory was full, so you threw away " + itemName + ".";
                generateAndSayResponse(context);
                return;
            }
        }
    }
    
    private void pickupNearbyItems() {
        if (isKnockedOut) return;
        List<ItemEntity> items = this.getEntityWorld().getEntitiesByClass(
            ItemEntity.class, 
            this.getBoundingBox().expand(1.0, 1.0, 1.0), 
            item -> !item.cannotPickup() && item.isAlive()
        );

        if (items.isEmpty()) return;

        List<String> pickedUpNames = new ArrayList<>();
        boolean pickedUpAny = false;

        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getStack();
            String itemName = stack.getName().getString();
            int count = stack.getCount();
            
            ItemStack remainder = this.inventory.addStack(stack);
            
            if (remainder.isEmpty()) {
                this.sendPickup(itemEntity, count);
                itemEntity.discard();
                pickedUpNames.add(count > 1 ? count + "x " + itemName : itemName);
                pickedUpAny = true;
            } else {
                int pickedUpCount = count - remainder.getCount();
                if (pickedUpCount > 0) {
                    pickedUpNames.add(pickedUpCount + "x " + itemName);
                    pickedUpAny = true;
                }
                itemEntity.setStack(remainder);
            }
        }

        if (pickedUpAny && ModConfig.get().enableAI && (pickedUpNames.size() >= 3 || Math.random() < 0.20)) {
            triggerPickupReaction(String.join(", ", pickedUpNames));
        }
    }

    private void triggerPickupReaction(String itemNames) {
        if (isGeneratingResponse || System.currentTimeMillis() - lastPhraseTime < SPEECH_COOLDOWN) return;
        generateAndSayResponse("You just picked up these items: " + itemNames + ".");
    }

    private void triggerDamageReaction(Entity attacker, boolean isSelf) {
        if (!ModConfig.get().enableAI || isKnockedOut) return;
        if (System.currentTimeMillis() - lastPhraseTime < 2000) return;

        this.lastDamageReactionTime = System.currentTimeMillis();
        String attackerName = (attacker != null) ? attacker.getName().getString() : "something";
        String prompt = isSelf 
            ? "You were attacked by " + attackerName + ". React with pain/anger."
            : "Your owner was attacked by " + attackerName + "! React with concern/anger.";

        generateAndSayResponse(prompt);
    }
    
    private void updateNameTag() {
        String displayName = "Girlfriend";
        if (!this.playerCustomName.isEmpty()) displayName = this.playerCustomName;
        displayName += " [Lv:" + this.relationshipLevel + "]";
        if (this.isKnockedOut) displayName += " (Unconscious)";
        this.setCustomName(Text.literal(displayName));
    }

    public void feedEntity(ItemStack stack) {
        if (isKnockedOut) return;
        if (stack.isOf(Items.APPLE) || stack.isOf(Items.GOLDEN_APPLE)) {
            this.setHealth(Math.min(this.getHealth() + 5.0f, this.getMaxHealth()));
            this.addRelationship(5);
        } else {
            this.setHealth(Math.min(this.getHealth() + 2.0f, this.getMaxHealth()));
            this.addRelationship(2);
        }
    }

    public void reactToItem(PlayerEntity player, ItemStack stack, boolean wasAddedToInventory) {
        if (!ModConfig.get().enableAI || isKnockedOut) return;
        String itemName = stack.getName().getString();
        getMemory().addMessage("system", "Player gave you " + itemName + (wasAddedToInventory ? "" : " (Inventory full)"));

        if (isGeneratingResponse || System.currentTimeMillis() - lastPhraseTime < SPEECH_COOLDOWN) return;

        String prompt = player.getName().getString() + " gave you " + itemName + (wasAddedToInventory ? ". Accepted." : ", but inventory full.");
        generateAndSayResponse(prompt);
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
    
    public void toggle() {
        if (isKnockedOut) return;
        this.isFollowing = !this.isFollowing;
        if (this.owner != null) {
            String status = this.isFollowing ? "following you" : "waiting here";
            this.owner.sendMessage(Text.literal("♥ " + this.getName().getString() + ": i'm " + status), false);
        }
    }
}