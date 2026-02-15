package com.beckytidus.girlfriendmod.entity;

import com.beckytidus.girlfriendmod.ai.AIClientManager;
import com.beckytidus.girlfriendmod.ai.ChutesClient;
import com.beckytidus.girlfriendmod.ai.ConversationManager;
import com.beckytidus.girlfriendmod.ai.RelationshipManager;
import com.beckytidus.girlfriendmod.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;

import java.util.*;
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
    private static final long DAMAGE_REACTION_COOLDOWN = 6000; // 6 seconds
    private static final long INVENTORY_CHECK_COOLDOWN = 30000; // 30 seconds

    // Kill reaction logic
    private long lastKillReactionTime = 0;
    private static final long KILL_REACTION_COOLDOWN = 5000; // 5 seconds
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

    // Mob awareness system
    private long lastMobCheckTime = 0;
    private static final long MOB_CHECK_INTERVAL = 2000; // Check every 2 seconds
    private static final double REACTION_DISTANCE = 12.0; // Distance to detect mobs
    
    // Track current mob state for context and change detection
    private String currentMobSummary = "";
    private int lastHostileCount = 0;
    private int lastNeutralCount = 0;
    private long lastMobReactionTime = 0;
    private static final long MOB_REACTION_COOLDOWN = 15000; // 15 seconds between mob reactions

    // Biome awareness system
    private String currentBiomeName = "";
    private long lastBiomeCheckTime = 0;
    private static final long BIOME_CHECK_INTERVAL = 2000; // Check every 2 seconds
    private static final long BIOME_REACTION_COOLDOWN = 30000; // 30 seconds between biome reactions
    private long lastBiomeReactionTime = 0;

    private String playerCustomName = "";

    private PlayerEntity owner;
    private UUID ownerUuid;

    private boolean isFollowing = true;

    // Inventory: 36 slots
    private final SimpleInventory inventory = new SimpleInventory(36);

    // AI Components
    private ConversationManager conversationManager;
    private RelationshipManager relationshipManager;
    private String gameContext = "Standing idly.";

    // Combat State
    private MeleeAttackGoal meleeAttackGoal;
    private ProjectileAttackGoal bowAttackGoal;
    private boolean wasUsingBow = false;

    // Door interaction state
    private BlockPos lastOpenedDoorPos = null;
    private long doorOpenTime = 0;
    private static final long DOOR_CLOSE_DELAY = 20L; // ticks before closing (~1 second)

    public enum GiveResult {
        SUCCESS,
        FULL,
        NOT_FOUND,
        PARTIAL
    }

    /**
     * Represents a request for a specific item with optional quantity.
     * quantity = null means give all of that item
     * quantity = specific number means give that many
     */
    public static class ItemRequest {
        public final String itemName;
        public final Integer quantity; // null = all

        public ItemRequest(String itemName, Integer quantity) {
            this.itemName = itemName;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return quantity == null ? itemName : quantity + "x " + itemName;
        }
    }

    /**
     * Result of giving multiple items.
     */
    public static class MultiGiveResult {
        public final Map<String, Integer> givenItems; // item name -> count given
        public final Map<String, Integer> failedItems; // item name -> count that couldn't be given
        public final List<String> notFoundItems;

        public MultiGiveResult() {
            this.givenItems = new HashMap<>();
            this.failedItems = new HashMap<>();
            this.notFoundItems = new ArrayList<>();
        }

        public boolean hasAnySuccess() {
            return !givenItems.isEmpty();
        }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            if (!givenItems.isEmpty()) {
                sb.append("Given: ");
                sb.append(givenItems.entrySet().stream()
                    .map(e -> e.getValue() + "x " + e.getKey())
                    .collect(java.util.stream.Collectors.joining(", ")));
            }
            if (!failedItems.isEmpty()) {
                if (sb.length() > 0) sb.append(". ");
                sb.append("Couldn't give (inventory full): ");
                sb.append(failedItems.entrySet().stream()
                    .map(e -> e.getValue() + "x " + e.getKey())
                    .collect(java.util.stream.Collectors.joining(", ")));
            }
            if (!notFoundItems.isEmpty()) {
                if (sb.length() > 0) sb.append(". ");
                sb.append("Not found: ");
                sb.append(String.join(", ", notFoundItems));
            }
            return sb.toString();
        }
    }

    public GirlFriendEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        // Get custom name from config
        ModConfig config = ModConfig.get();
        String displayName = config.customName.isEmpty() ? "Girlfriend" : config.customName;
        this.setCustomName(Text.literal(displayName));
        this.playerCustomName = displayName; // Store for later use

        this.inventory.addListener(inv -> {});
        this.setCanPickUpLoot(false);
        this.relationshipManager = new RelationshipManager(this);
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

        // Reinitialize relationship manager with loaded level
        this.relationshipManager = new RelationshipManager(this);
        this.relationshipManager.setRelationshipLevel(this.relationshipLevel);
    }

    public ConversationManager getMemory() {
        if (conversationManager == null) {
            conversationManager = new ConversationManager(this.getUuid());
        }
        return conversationManager;
    }

    public RelationshipManager getRelationshipManager() {
        return relationshipManager;
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
        this.goalSelector.add(1, new DoorInteractGoal());

        this.goalSelector.add(2, meleeAttackGoal);

        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8) {
            @Override
            public void start() {
                super.start();
                if (!GirlFriendEntity.this.gameContext.contains("fighting") &&
                    !GirlFriendEntity.this.gameContext.contains("attacked")) {
                    boolean isSwimming = GirlFriendEntity.this.isTouchingWater() || GirlFriendEntity.this.isMostlySubmergedInWater();
                    GirlFriendEntity.this.gameContext = isSwimming ? "Swimming." : "Wandering around.";
                }
            }

            @Override
            public void stop() {
                super.stop();
                // Only reset to idle if not in combat
                if (GirlFriendEntity.this.getTarget() == null) {
                    boolean isSwimming = GirlFriendEntity.this.isTouchingWater() || GirlFriendEntity.this.isMostlySubmergedInWater();
                    GirlFriendEntity.this.gameContext = isSwimming ? "Swimming." : "Standing idly.";
                }
            }
        });
        this.goalSelector.add(6, new FollowOwnerGoal());

        this.targetSelector.add(1, new ForgivingRevengeGoal());
        this.targetSelector.add(2, new OwnerSupportGoal());
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    // Custom RevengeGoal that excludes owner if relationship is high enough
    private class ForgivingRevengeGoal extends RevengeGoal {
        public ForgivingRevengeGoal() {
            super(GirlFriendEntity.this);
        }

        @Override
        public boolean canStart() {
            if (!super.canStart()) return false;

            // Check if the attacker is the owner
            LivingEntity attacker = GirlFriendEntity.this.getAttacker();
            if (attacker == GirlFriendEntity.this.owner) {
                // Owner attacked me - only retaliate if relationship is very low
                return !relationshipManager.willForgiveAccidentalHits();
            }

            return true;
        }
    }

    // --- Mob Awareness System ---
    private void tickMobAwareness() {
        if (this.isKnockedOut) return;
        if (!ModConfig.get().enableAI) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMobCheckTime < MOB_CHECK_INTERVAL) return;

        lastMobCheckTime = currentTime;

        // Check for nearby mobs
        List<Entity> nearbyMobs = this.getEntityWorld().getOtherEntities(
            this,
            this.getBoundingBox().expand(REACTION_DISTANCE),
            entity -> entity instanceof LivingEntity &&
                      !(entity instanceof PlayerEntity) &&
                      !(entity instanceof GirlFriendEntity) &&
                      entity.isAlive()
        );

        // Count mobs by type and hostility
        Map<String, Integer> hostileMobs = new HashMap<>();
        Map<String, Integer> neutralMobs = new HashMap<>();
        
        for (Entity entity : nearbyMobs) {
            String mobName = entity.getName().getString();
            boolean isHostile = entity instanceof Monster || entity instanceof HostileEntity;
            
            if (isHostile) {
                hostileMobs.merge(mobName, 1, Integer::sum);
            } else {
                neutralMobs.merge(mobName, 1, Integer::sum);
            }
        }
        
        int totalHostile = hostileMobs.values().stream().mapToInt(Integer::intValue).sum();
        int totalNeutral = neutralMobs.values().stream().mapToInt(Integer::intValue).sum();
        
        // Build mob summary for context
        StringBuilder summaryBuilder = new StringBuilder();
        if (!hostileMobs.isEmpty()) {
            summaryBuilder.append("Hostile: ");
            summaryBuilder.append(hostileMobs.entrySet().stream()
                .map(e -> e.getValue() + "x " + e.getKey())
                .collect(Collectors.joining(", ")));
        }
        if (!neutralMobs.isEmpty()) {
            if (summaryBuilder.length() > 0) summaryBuilder.append(". ");
            summaryBuilder.append("Neutral: ");
            summaryBuilder.append(neutralMobs.entrySet().stream()
                .map(e -> e.getValue() + "x " + e.getKey())
                .collect(Collectors.joining(", ")));
        }
        
        String newMobSummary = summaryBuilder.toString();
        
        // Check if mob composition has changed significantly
        boolean hostileChanged = Math.abs(totalHostile - lastHostileCount) >= 2 || 
                                 (lastHostileCount == 0 && totalHostile > 0);
        boolean neutralChanged = Math.abs(totalNeutral - lastNeutralCount) >= 3;
        
        // Update the mob summary for context
        this.currentMobSummary = newMobSummary;
        
        // React to changes (with cooldown)
        boolean canReact = currentTime - lastMobReactionTime >= MOB_REACTION_COOLDOWN &&
                          !isGeneratingResponse &&
                          currentTime - lastPhraseTime >= SPEECH_COOLDOWN;
        
        if (canReact) {
            // Hostile mobs appeared or changed significantly - higher chance to react
            if (hostileChanged && totalHostile > 0) {
                if (this.random.nextFloat() < 0.40f) { // 40% chance for hostiles
                    triggerMobSummaryReaction(hostileMobs, neutralMobs, totalHostile, totalNeutral, true);
                    lastMobReactionTime = currentTime;
                }
            }
            // Neutral mobs changed significantly - lower chance to react
            else if (neutralChanged && totalNeutral > 0 && totalHostile == 0) {
                if (this.random.nextFloat() < 0.10f) { // 10% chance for neutrals
                    triggerMobSummaryReaction(hostileMobs, neutralMobs, totalHostile, totalNeutral, false);
                    lastMobReactionTime = currentTime;
                }
            }
        }
        
        // Update last counts
        lastHostileCount = totalHostile;
        lastNeutralCount = totalNeutral;
    }

    // --- Biome Awareness System ---
    private void tickBiomeAwareness() {
        if (this.isKnockedOut) return;
        if (this.getEntityWorld().isClient()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBiomeCheckTime < BIOME_CHECK_INTERVAL) return;

        lastBiomeCheckTime = currentTime;

        // Get the current biome
        try {
            RegistryEntry<Biome> biomeEntry = this.getEntityWorld().getBiome(this.getBlockPos());
            String newBiomeName = getBiomeDisplayName(biomeEntry);

            // Check if biome has changed
            if (!newBiomeName.equals(this.currentBiomeName)) {
                String oldBiomeName = this.currentBiomeName;
                this.currentBiomeName = newBiomeName;

                // Only react if we had a previous biome (not on first check)
                boolean canReact = !oldBiomeName.isEmpty() &&
                                   currentTime - lastBiomeReactionTime >= BIOME_REACTION_COOLDOWN &&
                                   !isGeneratingResponse &&
                                   currentTime - lastPhraseTime >= SPEECH_COOLDOWN;

                if (canReact) {
                    triggerBiomeChangeReaction(oldBiomeName, newBiomeName);
                    lastBiomeReactionTime = currentTime;
                }
            }
        } catch (Exception e) {
            // Silently ignore biome detection errors
        }
    }

    private String getBiomeDisplayName(RegistryEntry<Biome> biomeEntry) {
        try {
            // Try to get the registry key
            Optional<RegistryKey<Biome>> key = biomeEntry.getKey();
            if (key.isPresent()) {
                String biomeId = key.get().getValue().toString();
                // Convert "minecraft:plains" to "Plains"
                String name = biomeId.replace("minecraft:", "");
                // Convert snake_case to Title Case
                StringBuilder result = new StringBuilder();
                boolean capitalizeNext = true;
                for (char c : name.toCharArray()) {
                    if (c == '_') {
                        result.append(' ');
                        capitalizeNext = true;
                    } else {
                        if (capitalizeNext) {
                            result.append(Character.toUpperCase(c));
                            capitalizeNext = false;
                        } else {
                            result.append(c);
                        }
                    }
                }
                return result.toString();
            }
        } catch (Exception e) {
            // Fall through
        }
        return "Unknown";
    }

    private void triggerBiomeChangeReaction(String oldBiome, String newBiome) {
        if (!ModConfig.get().enableAI) return;

        String name = getNameForContext();

        // Add biome change event to history
        String biomeEvent = name + " entered " + newBiome + " biome.";
        getMemory().addMessage("system", biomeEvent);

        // Generate reaction to entering new biome
        String prompt = name + " just walked from " + oldBiome + " into " + newBiome + 
            ". React briefly to the new environment - notice the change in scenery, temperature, or atmosphere.";
        generateAndSayResponse(prompt);
    }

    private void triggerMobSummaryReaction(Map<String, Integer> hostileMobs, Map<String, Integer> neutralMobs, 
                                           int totalHostile, int totalNeutral, boolean isHostileTrigger) {
        String name = getNameForContext();
        
        // Build a natural language summary
        StringBuilder reactionBuilder = new StringBuilder();
        
        if (isHostileTrigger && !hostileMobs.isEmpty()) {
            reactionBuilder.append(name).append(" noticed ");
            if (hostileMobs.size() == 1) {
                Map.Entry<String, Integer> entry = hostileMobs.entrySet().iterator().next();
                reactionBuilder.append(entry.getValue()).append("x ").append(entry.getKey());
            } else {
                reactionBuilder.append("hostile creatures nearby: ");
                reactionBuilder.append(hostileMobs.entrySet().stream()
                    .map(e -> e.getValue() + "x " + e.getKey())
                    .collect(Collectors.joining(", ")));
            }
            reactionBuilder.append(".");
        } else if (!neutralMobs.isEmpty()) {
            reactionBuilder.append(name).append(" noticed ");
            if (neutralMobs.size() == 1) {
                Map.Entry<String, Integer> entry = neutralMobs.entrySet().iterator().next();
                reactionBuilder.append(entry.getValue() > 1 ? entry.getValue() + "x " : "").append(entry.getKey());
            } else {
                reactionBuilder.append("some animals nearby: ");
                reactionBuilder.append(neutralMobs.entrySet().stream()
                    .map(e -> e.getValue() + "x " + e.getKey())
                    .collect(Collectors.joining(", ")));
            }
            reactionBuilder.append(".");
        }
        
        String mobEvent = reactionBuilder.toString();
        
        // Add to memory
        getMemory().addMessage("system", mobEvent);
        
        // Update context
        if (totalHostile > 0) {
            updateGameContext("Nearby hostiles detected: " + hostileMobs.keySet());
        }
        
        // Generate appropriate reaction
        String prompt;
        if (totalHostile > 0) {
            prompt = name + " noticed hostile creatures nearby (" + 
                hostileMobs.entrySet().stream()
                    .map(e -> e.getValue() + "x " + e.getKey())
                    .collect(Collectors.joining(", ")) + 
                "). React with caution or concern about the danger.";
        } else {
            prompt = name + " noticed some passive animals nearby (" + 
                neutralMobs.entrySet().stream()
                    .map(e -> e.getValue() + "x " + e.getKey())
                    .collect(Collectors.joining(", ")) + 
                "). React briefly with mild interest or just acknowledge them.";
        }
        
        generateAndSayResponse(prompt);
    }

    // --- Combat Logic ---

    private void tickCombatLogic() {
        if (this.isKnockedOut) return;  // Don't do combat logic when knocked out

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
        if (this.isKnockedOut) return; // Can't shoot when knocked out

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
        if (this.isKnockedOut) return false; // Can't kill when knocked out

        boolean result = super.onKilledOther(world, other, source);

        // Add kill event to chat history - specify that GIRLFRIEND killed it
        String name = getNameForContext();
        String killEvent = name + " personally defeated " + other.getName().getString() + ".";
        getMemory().addMessage("system", killEvent);

        // Update context
        updateGameContext("Just personally defeated " + other.getName().getString());

        // Direct kill reaction
        triggerKillReaction(other);
        return result;
    }

    // NEW: Handle when owner kills a mob
    public void onOwnerKilledMob(LivingEntity mob) {
        if (!ModConfig.get().enableAI || isKnockedOut) return;

        long currentTime = System.currentTimeMillis();
        // Cooldown check for owner kills
        if (currentTime - lastKillReactionTime < KILL_REACTION_COOLDOWN) return;

        // Don't interrupt if already speaking recently
        if (isGeneratingResponse || currentTime - lastPhraseTime < SPEECH_COOLDOWN) return;

        this.lastKillReactionTime = currentTime;
        String mobName = mob.getName().getString();
        String name = getNameForContext();

        // Check if girlfriend was also targeting this mob
        boolean wasHelping = this.getTarget() == mob || this.monitoredTarget == mob;

        // Add kill event to chat history as a system message
        String killEvent = "Owner killed " + mobName + (wasHelping ? " with " + name + "'s help." : ".");
        getMemory().addMessage("system", killEvent);

        // Update context for immediate reaction
        updateGameContext("Owner just killed " + mobName + (wasHelping ? " with my help" : ""));

        // Contextual Reaction - different based on whether she was helping
        if (wasHelping) {
            generateAndSayResponse(name + " helped owner defeat " + mobName + ". react with teamwork praise or shared victory.");
        } else {
            generateAndSayResponse("Owner just killed " + mobName + " nearby. react to seeing them fight.");
        }
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
        String name = getNameForContext();

        // Add kill event to chat history as a system message
        String killEvent = name + " personally defeated " + enemyName + ".";
        getMemory().addMessage("system", killEvent);

        // Update context for immediate reaction
        updateGameContext("Just personally defeated " + enemyName);

        // Contextual Reaction - emphasize that SHE killed it
        generateAndSayResponse(name + " just personally defeated " + enemyName + "! react with personal victory.");
    }

    private class OwnerSupportGoal extends Goal {
        private static final double ENGAGE_DISTANCE = 8.0; // Only engage when mob is close

        public OwnerSupportGoal() {
            this.setControls(EnumSet.of(Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (owner == null || isKnockedOut) return false;

            // Check if owner is being attacked or attacking
            LivingEntity target = owner.getAttacker();
            if (target == null) {
                target = owner.getAttacking();
            }

            // Only engage if target is close enough
            if (target != null && target.isAlive() &&
                !(target instanceof PlayerEntity) &&
                !(target instanceof GirlFriendEntity)) {

                double distance = GirlFriendEntity.this.distanceTo(target);
                return distance <= ENGAGE_DISTANCE;
            }

            return false;
        }

        @Override
        public void start() {
            LivingEntity target = owner.getAttacker();
            if (target == null) {
                target = owner.getAttacking();
            }

            if (target != null && target.isAlive() && !(target instanceof PlayerEntity) && !(target instanceof GirlFriendEntity)) {
                GirlFriendEntity.this.setTarget(target);

                // Add combat start event to history
                String name = getNameForContext();
                String combatEvent = name + " started fighting " + target.getName().getString() + " to protect owner.";
                getMemory().addMessage("system", combatEvent);

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

            // Check if swimming
            boolean isSwimming = GirlFriendEntity.this.isTouchingWater() || GirlFriendEntity.this.isMostlySubmergedInWater();

            boolean shouldSprint = owner.isSprinting() || distance > MAX_DISTANCE_TO_WALK;
            GirlFriendEntity.this.setSprinting(shouldSprint);

            if (distance > MIN_DISTANCE) {
                GirlFriendEntity.this.getNavigation().startMovingTo(owner, 1.0);

                // Update context based on swimming state
                if (!GirlFriendEntity.this.gameContext.contains("fighting") &&
                    !GirlFriendEntity.this.gameContext.contains("attacked")) {
                    if (isSwimming) {
                        GirlFriendEntity.this.gameContext = "Swimming to follow owner.";
                    } else {
                        GirlFriendEntity.this.gameContext = "Walking to follow owner.";
                    }
                }
            } else {
                GirlFriendEntity.this.getNavigation().stop();
                if (!owner.isSprinting()) {
                    GirlFriendEntity.this.setSprinting(false);
                }
            }
        }
    }

    // Door/Trapdoor/FenceGate interaction goal - opens and closes when following
    private class DoorInteractGoal extends Goal {
        private static final double DOOR_DETECT_RANGE = 2.5;

        public DoorInteractGoal() {
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (isKnockedOut) return false;
            if (owner == null || !isFollowing) return false;
            if (GirlFriendEntity.this.getTarget() != null) return false; // Don't interact with doors during combat

            // Check if we're close to a door, trapdoor, or fence gate (either closed or open)
            return findNearbyClosedInteractable() != null || findNearbyOpenInteractable() != null;
        }

        @Override
        public boolean shouldContinue() {
            return !isKnockedOut && lastOpenedDoorPos != null;
        }

        @Override
        public void tick() {
            // Check if we need to close a previously opened/passed block
            if (lastOpenedDoorPos != null) {
                long currentTime = GirlFriendEntity.this.age;
                double distanceToBlock = GirlFriendEntity.this.squaredDistanceTo(lastOpenedDoorPos.getX() + 0.5, lastOpenedDoorPos.getY(), lastOpenedDoorPos.getZ() + 0.5);

                // Close the block after delay AND when we've moved away from it
                if (currentTime - doorOpenTime >= DOOR_CLOSE_DELAY && distanceToBlock > 2.0) {
                    closeInteractable(lastOpenedDoorPos);
                    lastOpenedDoorPos = null;
                }
            }

            // Check for closed doors/trapdoors/fence gates to open
            BlockPos closedBlockPos = findNearbyClosedInteractable();
            if (closedBlockPos != null && !closedBlockPos.equals(lastOpenedDoorPos)) {
                openInteractable(closedBlockPos);
            }

            // Check for already open doors/trapdoors/fence gates to track for closing
            BlockPos openBlockPos = findNearbyOpenInteractable();
            if (openBlockPos != null && !openBlockPos.equals(lastOpenedDoorPos)) {
                // Track this open door so we can close it after passing through
                lastOpenedDoorPos = openBlockPos;
                doorOpenTime = GirlFriendEntity.this.age;
            }
        }

        private BlockPos findNearbyClosedInteractable() {
            BlockPos entityPos = GirlFriendEntity.this.getBlockPos();
            World world = GirlFriendEntity.this.getEntityWorld();

            // Check blocks around the entity for CLOSED doors, trapdoors, and fence gates
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        BlockPos checkPos = entityPos.add(dx, dy, dz);
                        BlockState state = world.getBlockState(checkPos);

                        // Check for doors
                        if (state.getBlock() instanceof DoorBlock) {
                            if (state.contains(DoorBlock.OPEN)) {
                                boolean isOpen = state.get(DoorBlock.OPEN);

                                // Only return closed doors
                                if (!isOpen) {
                                    // Make sure it's the lower half of the door
                                    if (state.contains(DoorBlock.HALF) && state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                                        return checkPos;
                                    }
                                }
                            }
                        }

                        // Check for trapdoors
                        if (state.getBlock() instanceof TrapdoorBlock) {
                            if (state.contains(TrapdoorBlock.OPEN)) {
                                boolean isOpen = state.get(TrapdoorBlock.OPEN);
                                if (!isOpen) {
                                    return checkPos;
                                }
                            }
                        }

                        // Check for fence gates
                        if (state.getBlock() instanceof FenceGateBlock) {
                            if (state.contains(FenceGateBlock.OPEN)) {
                                boolean isOpen = state.get(FenceGateBlock.OPEN);
                                if (!isOpen) {
                                    return checkPos;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private BlockPos findNearbyOpenInteractable() {
            BlockPos entityPos = GirlFriendEntity.this.getBlockPos();
            World world = GirlFriendEntity.this.getEntityWorld();

            // Check blocks around the entity for OPEN doors, trapdoors, and fence gates
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        BlockPos checkPos = entityPos.add(dx, dy, dz);
                        BlockState state = world.getBlockState(checkPos);

                        // Check for doors
                        if (state.getBlock() instanceof DoorBlock) {
                            if (state.contains(DoorBlock.OPEN)) {
                                boolean isOpen = state.get(DoorBlock.OPEN);

                                // Return open doors
                                if (isOpen) {
                                    // Make sure it's the lower half of the door
                                    if (state.contains(DoorBlock.HALF) && state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                                        return checkPos;
                                    }
                                }
                            }
                        }

                        // Check for trapdoors
                        if (state.getBlock() instanceof TrapdoorBlock) {
                            if (state.contains(TrapdoorBlock.OPEN)) {
                                boolean isOpen = state.get(TrapdoorBlock.OPEN);
                                if (isOpen) {
                                    return checkPos;
                                }
                            }
                        }

                        // Check for fence gates
                        if (state.getBlock() instanceof FenceGateBlock) {
                            if (state.contains(FenceGateBlock.OPEN)) {
                                boolean isOpen = state.get(FenceGateBlock.OPEN);
                                if (isOpen) {
                                    return checkPos;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        private void openInteractable(BlockPos blockPos) {
            World world = GirlFriendEntity.this.getEntityWorld();
            BlockState state = world.getBlockState(blockPos);

            // Handle doors
            if (state.getBlock() instanceof DoorBlock && state.contains(DoorBlock.OPEN)) {
                BlockState newState = state.with(DoorBlock.OPEN, true);
                world.setBlockState(blockPos, newState, 10);
                GirlFriendEntity.this.playSound(SoundEvents.BLOCK_WOODEN_DOOR_OPEN, 1.0F, 1.0F);
                lastOpenedDoorPos = blockPos;
                doorOpenTime = GirlFriendEntity.this.age;
                return;
            }

            // Handle trapdoors
            if (state.getBlock() instanceof TrapdoorBlock && state.contains(TrapdoorBlock.OPEN)) {
                BlockState newState = state.with(TrapdoorBlock.OPEN, true);
                world.setBlockState(blockPos, newState, 10);
                GirlFriendEntity.this.playSound(SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, 1.0F, 1.0F);
                lastOpenedDoorPos = blockPos;
                doorOpenTime = GirlFriendEntity.this.age;
                return;
            }

            // Handle fence gates
            if (state.getBlock() instanceof FenceGateBlock && state.contains(FenceGateBlock.OPEN)) {
                BlockState newState = state.with(FenceGateBlock.OPEN, true);
                world.setBlockState(blockPos, newState, 10);
                GirlFriendEntity.this.playSound(SoundEvents.BLOCK_FENCE_GATE_OPEN, 1.0F, 1.0F);
                lastOpenedDoorPos = blockPos;
                doorOpenTime = GirlFriendEntity.this.age;
                return;
            }
        }

        private void closeInteractable(BlockPos blockPos) {
            World world = GirlFriendEntity.this.getEntityWorld();
            BlockState state = world.getBlockState(blockPos);

            // Handle doors
            if (state.getBlock() instanceof DoorBlock && state.contains(DoorBlock.OPEN)) {
                boolean isOpen = state.get(DoorBlock.OPEN);
                if (isOpen) {
                    BlockState newState = state.with(DoorBlock.OPEN, false);
                    world.setBlockState(blockPos, newState, 10);
                    GirlFriendEntity.this.playSound(SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, 1.0F, 1.0F);
                }
                return;
            }

            // Handle trapdoors
            if (state.getBlock() instanceof TrapdoorBlock && state.contains(TrapdoorBlock.OPEN)) {
                boolean isOpen = state.get(TrapdoorBlock.OPEN);
                if (isOpen) {
                    BlockState newState = state.with(TrapdoorBlock.OPEN, false);
                    world.setBlockState(blockPos, newState, 10);
                    GirlFriendEntity.this.playSound(SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1.0F, 1.0F);
                }
                return;
            }

            // Handle fence gates
            if (state.getBlock() instanceof FenceGateBlock && state.contains(FenceGateBlock.OPEN)) {
                boolean isOpen = state.get(FenceGateBlock.OPEN);
                if (isOpen) {
                    BlockState newState = state.with(FenceGateBlock.OPEN, false);
                    world.setBlockState(blockPos, newState, 10);
                    GirlFriendEntity.this.playSound(SoundEvents.BLOCK_FENCE_GATE_CLOSE, 1.0F, 1.0F);
                }
                return;
            }
        }
    }

    public void updateGameContext(String ctx) {
        // Only update if we're not already in a more specific state
        // (like combat or recent events)
        if (this.gameContext.equals("Standing idly.") ||
            this.gameContext.equals("Walking around.") ||
            this.gameContext.equals("Swimming.") ||
            this.gameContext.equals("Wandering around.") ||
            this.gameContext.equals("Walking to follow owner.") ||
            this.gameContext.equals("Swimming to follow owner.") ||
            ctx.contains("defeated") ||
            ctx.contains("fighting") ||
            ctx.contains("attacked") ||
            ctx.contains("picked up") ||
            ctx.contains("gave")) {
            this.gameContext = ctx;
        }
    }

    /**
     * Get the cardinal direction the entity is facing.
     * Minecraft yaw: 0=South, 90=West, 180=North, 270=East
     */
    private String getFacingDirection() {
        float yaw = this.getYaw() % 360;
        if (yaw < 0) yaw += 360;
        
        // Use 8 cardinal directions for more precision
        if (yaw >= 337.5 || yaw < 22.5) return "South";
        else if (yaw >= 22.5 && yaw < 67.5) return "Southwest";
        else if (yaw >= 67.5 && yaw < 112.5) return "West";
        else if (yaw >= 112.5 && yaw < 157.5) return "Northwest";
        else if (yaw >= 157.5 && yaw < 202.5) return "North";
        else if (yaw >= 202.5 && yaw < 247.5) return "Northeast";
        else if (yaw >= 247.5 && yaw < 292.5) return "East";
        else return "Southeast"; // 292.5 to 337.5
    }

    private String buildSystemContext(String specificActionContext) {
        StringBuilder sb = new StringBuilder();
        String name = getNameForContext();

        if (specificActionContext != null && !specificActionContext.isEmpty()) {
            // Replace "you" with the name in the specific action context
            String modifiedContext = specificActionContext
                .replace("you ", name + " ")
                .replace("You ", name + " ")
                .replace("your ", name + "'s ")
                .replace("Your ", name + "'s ");
            sb.append(modifiedContext).append(". ");
        }

        long time = this.getEntityWorld().getTimeOfDay() % 24000;
        String timeDesc = "Day";
        if (time >= 23000 || time < 1000) timeDesc = "Sunrise/Morning";
        else if (time >= 12000 && time < 13000) timeDesc = "Sunset/Dusk";
        else if (time >= 13000) timeDesc = "Night";

        sb.append("Time: ").append(timeDesc).append(" (").append(time).append("). ");

        // Add movement/state information
        sb.append("Current Activity: ").append(this.gameContext).append(". ");

        // Add girlfriend's position and facing direction
        sb.append(name).append("'s Position: X: ").append(String.format("%.0f", this.getX()))
          .append(", Y: ").append(String.format("%.0f", this.getY()))
          .append(", Z: ").append(String.format("%.0f", this.getZ()))
          .append(". Facing: ").append(getFacingDirection()).append(". ");

        // Add owner's position if available
        if (this.owner != null) {
            sb.append("Owner's Position: X: ").append(String.format("%.0f", this.owner.getX()))
              .append(", Y: ").append(String.format("%.0f", this.owner.getY()))
              .append(", Z: ").append(String.format("%.0f", this.owner.getZ())).append(". ");
            
            double distance = this.distanceTo(this.owner);
            sb.append("Distance from owner: ").append(String.format("%.1f", distance)).append(" blocks. ");
        }

        sb.append(getInventoryContextString()).append(". ");
        sb.append(name).append("'s Status - Health: ").append((int)this.getHealth()).append("/").append((int)this.getMaxHealth());
        sb.append(", Armor: ").append(this.getArmor()).append(". ");
        sb.append("Relationship: ").append(this.relationshipLevel).append("/100. ");

        if (this.owner != null) {
            sb.append("Owner Status - Health: ").append((int)this.owner.getHealth()).append("/").append((int)this.owner.getMaxHealth());
            sb.append(", Armor: ").append(this.owner.getArmor()).append(" (20/20 is full for normal players). ");
        }

        if (this.isKnockedOut) {
            sb.append("STATUS: KNOCKED OUT / UNCONSCIOUS. ");
        }

        // Add weather info if available
        if (this.getEntityWorld().isRaining()) {
            sb.append("It is raining. ");
        } else if (this.getEntityWorld().isThundering()) {
            sb.append("There is a thunderstorm. ");
        }

        // Add nearby mob summary if any mobs are nearby
        if (!this.currentMobSummary.isEmpty()) {
            sb.append("Nearby Mobs: ").append(this.currentMobSummary).append(". ");
        }

        // Add current biome if available
        if (!this.currentBiomeName.isEmpty()) {
            sb.append("Current Biome: ").append(this.currentBiomeName).append(". ");
        }

        return sb.toString();
    }

    // --- Combat and Knockout Overrides ---

    @Override
    public boolean isPushable() {
        return !this.isKnockedOut && super.isPushable();
    }

    public boolean canOpenDoors() {
        return true;
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return !this.isKnockedOut && super.canTarget(target);
    }

    @Override
    public boolean canTakeDamage() {
        return !this.isKnockedOut && super.canTakeDamage();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isKnockedOut) {
            return false; // Can't take damage when knocked out
        }

        if (this.isInvulnerableTo(world, source)) {
            return false;
        }

        // Check if attacker is owner
        Entity attacker = source.getAttacker();
        if (attacker == this.owner) {
            // Owner hit me - check relationship
            if (relationshipManager.willForgiveAccidentalHits()) {
                // Reduce damage from owner if relationship is good
                amount *= (float) relationshipManager.getDamageReductionMultiplier();

                // Only take partial damage
                boolean damaged = super.damage(world, source, amount);

                if (damaged) {
                    // Use LLM to generate response instead of hardcoded message
                    String name = getNameForContext();
                    String context = name + " was accidentally hit by owner. " + name + " should express that it hurt but forgive them since it was an accident.";

                    // Add to memory
                    getMemory().addMessage("system", name + " was accidentally hit by owner. Taking reduced damage due to high relationship.");

                    // Generate AI response
                    if (!isGeneratingResponse && System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {
                        generateAndSayResponse(context);
                    }
                }
                return damaged;
            } else {
                // Relationship is low - treat as hostile
                this.setTarget((LivingEntity) attacker);

                // Add to memory
                String name = getNameForContext();
                getMemory().addMessage("system", name + " was attacked by owner. Relationship is too low to forgive.");

                // Generate AI response
                if (!isGeneratingResponse && System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {
                    generateAndSayResponse(name + " was attacked by owner! Express hurt and betrayal.");
                }
            }
        }

        if (this.getHealth() - amount <= 0) {
            startKnockoutProcess(source);
            return false;
        }

        // Apply damage reduction based on relationship
        amount *= (float) relationshipManager.getDamageReductionMultiplier();

        boolean damaged = super.damage(world, source, amount);

        if (damaged) {
            if (attacker != null && attacker != this.owner) { // Only react to non-owner attacks
                updateGameContext("Attacked by " + attacker.getName().getString());
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDamageReactionTime > DAMAGE_REACTION_COOLDOWN) {
                    triggerDamageReaction(attacker, true);
                }
            }

            if (this.owner != null && attacker instanceof PlayerEntity && attacker != this.owner) {
                this.addRelationship(-5);
            }
        }

        return damaged;
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        float damage = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);

        // Apply relationship-based damage multiplier
        damage *= (float) relationshipManager.getCombatDamageMultiplier();

        // Store original damage to restore after attack
        float originalDamage = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(damage);

        boolean attacked = super.tryAttack(world, target);

        // Restore original damage value
        this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(originalDamage);

        return attacked;
    }

    private void startKnockoutProcess(DamageSource source) {
        this.isKnockedOut = true;
        this.setHealth(1.0f);
        this.respawnTimer = RESPAWN_DELAY_TICKS;

        // Disable all combat and movement
        this.setTarget(null);
        this.getNavigation().stop();
        this.setSprinting(false);

        // Make invulnerable and invisible
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, RESPAWN_DELAY_TICKS, 0, false, false));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, RESPAWN_DELAY_TICKS, 10, false, false));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, RESPAWN_DELAY_TICKS, 100, false, false)); // Make invulnerable

        String attackerName = (source.getAttacker() != null) ? source.getAttacker().getName().getString() : "something";
        String name = getNameForContext();

        // Add knockout event to history
        getMemory().addMessage("system", name + " was knocked out by " + attackerName + ".");

        generateAndSayResponse(name + " has been defeated by " + attackerName + " and is falling unconscious. Say a dramatic goodbye.");

        if (this.owner != null) {
            this.owner.sendMessage(Text.literal("⚠ " + name + " has been knocked out! She will respawn in 20 seconds.").formatted(Formatting.RED), true);
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
        this.removeStatusEffect(StatusEffects.RESISTANCE);

        if (this.owner != null) {
            this.requestTeleport(this.owner.getX(), this.getY(), this.getZ());
        }

        String name = getNameForContext();

        // Add respawn event to history
        getMemory().addMessage("system", name + " respawned after being knocked out.");

        generateAndSayResponse(name + " has just regained consciousness and respawned near " + name + "'s owner. Express relief or happiness to be back.");

        if (this.owner != null) {
            this.owner.sendMessage(Text.literal("♥ " + name + " has revived!").formatted(Formatting.GREEN), true);
        }
    }

    // --- External Owner Events ---

    public void onOwnerDied(PlayerEntity owner) {
        if (hasReactedToOwnerDeath) return;
        hasReactedToOwnerDeath = true;

        String name = getNameForContext();

        // Add owner death event to history
        getMemory().addMessage("system", name + "'s owner died.");

        generateAndSayResponse(name + "'s owner just died right in front of " + name + "! Scream or cry out in shock.");
    }

    public void onOwnerRespawned(PlayerEntity newOwnerEntity) {
        this.setOwner(newOwnerEntity);
        this.hasReactedToOwnerDeath = false;

        this.requestTeleport(newOwnerEntity.getX(), newOwnerEntity.getY(), newOwnerEntity.getZ());

        this.setHealth(this.getMaxHealth());

        String name = getNameForContext();

        // Add owner respawn event to history
        getMemory().addMessage("system", name + "'s owner respawned.");

        generateAndSayResponse(name + "'s owner has just respawned/came back to life. " + name + " is teleported to them. Express joy and relief that they are okay.");
    }

    // --- Standard Methods ---

    public void processPlayerChat(String msg) {
        if (!ModConfig.get().enableAI) return;
        if (isKnockedOut) return;

        getMemory().addMessage("user", msg);
        relationshipManager.processInteraction(msg, getMemory().getRecentHistory(6));
    }

    public void generateAndSayResponse(String promptContext) {
        if (isGeneratingResponse) return;
        isGeneratingResponse = true;

        String fullContext = buildSystemContext(promptContext);
        String playerName = getOwnerNameForContext();

        AIClientManager.generateResponse(getMemory().getContextWindow(), fullContext, playerName)
            .thenAccept(response -> {
                isGeneratingResponse = false;
                this.lastPhraseTime = System.currentTimeMillis();

                // Add the AI response to chat history
                getMemory().addMessage("assistant", response);

                // Clear the current context after response is generated
                // But set appropriate default based on current state
                boolean isSwimming = this.isTouchingWater() || this.isMostlySubmergedInWater();
                boolean isMoving = this.getNavigation().isFollowingPath() ||
                                  this.getVelocity().horizontalLengthSquared() > 0.01;

                if (isSwimming) {
                    if (this.isFollowing && this.owner != null && this.distanceTo(this.owner) > 3.0) {
                        this.gameContext = "Swimming to follow owner.";
                    } else {
                        this.gameContext = "Swimming.";
                    }
                } else if (isMoving) {
                    if (this.isFollowing && this.owner != null && this.distanceTo(this.owner) > 3.0) {
                        this.gameContext = "Walking to follow owner.";
                    } else {
                        this.gameContext = "Walking around.";
                    }
                } else {
                    this.gameContext = "Standing idly.";
                }

                if (this.owner != null) {
                    this.owner.sendMessage(Text.literal("<" + this.getName().getString() + "> " + response).formatted(Formatting.LIGHT_PURPLE), false);
                }
            }).exceptionally(e -> {
                isGeneratingResponse = false;
                // Reset context based on current state
                boolean isSwimming = this.isTouchingWater() || this.isMostlySubmergedInWater();
                boolean isMoving = this.getNavigation().isFollowingPath() ||
                                  this.getVelocity().horizontalLengthSquared() > 0.01;

                if (isSwimming) {
                    if (this.isFollowing && this.owner != null && this.distanceTo(this.owner) > 3.0) {
                        this.gameContext = "Swimming to follow owner.";
                    } else {
                        this.gameContext = "Swimming.";
                    }
                } else if (isMoving) {
                    if (this.isFollowing && this.owner != null && this.distanceTo(this.owner) > 3.0) {
                        this.gameContext = "Walking to follow owner.";
                    } else {
                        this.gameContext = "Walking around.";
                    }
                } else {
                    this.gameContext = "Standing idly.";
                }
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

    public List<String> getItemNamesFromInventory() {
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
        return giveSpecificItem(targetItemName, 1);
    }

    /**
     * Give a specific quantity of an item to the owner.
     * @param targetItemName The name of the item to give
     * @param quantity The quantity to give, or null to give all
     * @return GiveResult indicating success, failure, or partial success
     */
    public GiveResult giveSpecificItem(String targetItemName, Integer quantity) {
        if (this.owner == null || this.inventory.isEmpty()) return GiveResult.NOT_FOUND;

        int totalAvailable = 0;
        List<Integer> matchingSlots = new ArrayList<>();

        // First, find all matching items and their total count
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                String stackName = stack.getName().getString();
                if (stackName.equalsIgnoreCase(targetItemName) || stackName.toLowerCase().contains(targetItemName.toLowerCase())) {
                    totalAvailable += stack.getCount();
                    matchingSlots.add(i);
                }
            }
        }

        if (matchingSlots.isEmpty()) {
            return GiveResult.NOT_FOUND;
        }

        // Determine how many to give
        int toGiveCount = quantity == null ? totalAvailable : Math.min(quantity, totalAvailable);

        if (toGiveCount <= 0) {
            return GiveResult.NOT_FOUND;
        }

        // Check if player inventory can accept all items
        int canAccept = 0;
        for (int slot : matchingSlots) {
            ItemStack stack = inventory.getStack(slot);
            ItemStack testStack = stack.copy();
            testStack.setCount(Math.min(toGiveCount - canAccept, stack.getCount()));
            // Simulate insertion by checking if we can add to player inventory
            int remaining = this.owner.getInventory().getEmptySlot() != -1 ? testStack.getCount() : 0;
            // More accurate check: try to insert and see remainder
            ItemStack testCopy = testStack.copy();
            boolean canInsertSome = false;
            for (int i = 0; i < this.owner.getInventory().size(); i++) {
                ItemStack playerStack = this.owner.getInventory().getStack(i);
                if (playerStack.isEmpty()) {
                    canAccept += testCopy.getCount();
                    canInsertSome = true;
                    break;
                } else if (ItemStack.areItemsEqual(playerStack, testCopy) && playerStack.getCount() < playerStack.getMaxCount()) {
                    int canFit = playerStack.getMaxCount() - playerStack.getCount();
                    int willTake = Math.min(canFit, testCopy.getCount());
                    canAccept += willTake;
                    testCopy.decrement(willTake);
                    if (testCopy.isEmpty()) {
                        canInsertSome = true;
                        break;
                    }
                }
            }
            if (!testCopy.isEmpty() && this.owner.getInventory().getEmptySlot() != -1) {
                canAccept += testCopy.getCount();
            }
            if (canAccept >= toGiveCount) break;
        }

        if (canAccept == 0) {
            return GiveResult.FULL;
        }

        // Actually give the items
        int remainingToGive = Math.min(toGiveCount, canAccept);
        int givenCount = 0;

        for (int slot : matchingSlots) {
            if (remainingToGive <= 0) break;

            ItemStack stack = inventory.getStack(slot);
            int takeFromThis = Math.min(remainingToGive, stack.getCount());

            ItemStack toGive = stack.copy();
            toGive.setCount(takeFromThis);

            if (this.owner.getInventory().insertStack(toGive)) {
                givenCount += takeFromThis;
                remainingToGive -= takeFromThis;
                stack.decrement(takeFromThis);
                if (stack.isEmpty()) {
                    inventory.setStack(slot, ItemStack.EMPTY);
                }
            }
        }

        if (givenCount == 0) {
            return GiveResult.FULL;
        } else if (givenCount < toGiveCount) {
            return GiveResult.PARTIAL;
        } else {
            return GiveResult.SUCCESS;
        }
    }

    /**
     * Give multiple items to the owner at once.
     * @param requests List of ItemRequest objects specifying items and quantities
     * @return MultiGiveResult with details of what was given, failed, or not found
     */
    public MultiGiveResult giveItems(List<ItemRequest> requests) {
        MultiGiveResult result = new MultiGiveResult();

        if (this.owner == null || this.inventory.isEmpty()) {
            for (ItemRequest request : requests) {
                result.notFoundItems.add(request.itemName);
            }
            return result;
        }

        for (ItemRequest request : requests) {
            // IMPORTANT: Get the count BEFORE giving the item (for "give all" case)
            int countBeforeGiving = getTotalCountOfItem(request.itemName);
            
            GiveResult giveResult = giveSpecificItem(request.itemName, request.quantity);

            // Track results - use the count we got BEFORE giving
            switch (giveResult) {
                case SUCCESS:
                    // For "give all" (null quantity), use the count we had before giving
                    // For specific quantity, use the requested quantity
                    int givenCount = request.quantity != null ? request.quantity : countBeforeGiving;
                    result.givenItems.put(request.itemName, givenCount);
                    break;
                case PARTIAL:
                    // Partial success - some items were given (player inventory was partially full)
                    // Use countBeforeGiving for "give all" case since we don't know exact amount
                    result.givenItems.put(request.itemName, request.quantity != null ? request.quantity : countBeforeGiving);
                    break;
                case FULL:
                    result.failedItems.put(request.itemName, request.quantity != null ? request.quantity : countBeforeGiving);
                    break;
                case NOT_FOUND:
                    result.notFoundItems.add(request.itemName);
                    break;
            }
        }

        return result;
    }

    /**
     * Get the total count of a specific item in the inventory.
     */
    private int getTotalCountOfItem(String itemName) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                String stackName = stack.getName().getString();
                if (stackName.equalsIgnoreCase(itemName) || stackName.toLowerCase().contains(itemName.toLowerCase())) {
                    count += stack.getCount();
                }
            }
        }
        return count;
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
        if (this.getHealth() < 10) prompt += " " + getNameForContext() + " is hurt.";
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
            // Check movement state every 20 ticks (1 second)
            if (this.age % 20 == 0) {
                updateMovementState();
            }

            if (this.age % 20 == 0) {
                pickupNearbyItems();
                tickTimeAwareness();
                tickCombatLogic();
                tickMobAwareness();
                tickBiomeAwareness(); // Biome change detection
                tickAutoHealOwner();
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
                this.heal(5.0f);
                lastHealTime = currentTime;
            }

            this.updateNameTag();
        }
    }

    // NEW: Auto-heal owner if relationship is high enough
    private void tickAutoHealOwner() {
        if (!relationshipManager.willAutoHealOwner()) return;
        if (this.owner == null) return;
        if (this.isKnockedOut) return;

        // Check if owner is hurt
        if (this.owner.getHealth() < this.owner.getMaxHealth() * 0.7f &&
            this.distanceTo(this.owner) < 10.0f) {

            // Check if we have healing items
            for (int i = 0; i < this.inventory.size(); i++) {
                ItemStack stack = this.inventory.getStack(i);
                if (stack.isEmpty()) continue;

                // Check for healing items (golden apples, potions, etc.)
                if (stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
                    // Use the item on owner
                    float healAmount = 4.0f * (float) relationshipManager.getHealingMultiplier();
                    this.owner.heal(healAmount);
                    stack.decrement(1);
                    if (stack.isEmpty()) {
                        this.inventory.setStack(i, ItemStack.EMPTY);
                    }

                    // Add to memory
                    getMemory().addMessage("system",
                        getNameForContext() + " used a golden apple to heal owner for " + String.format("%.1f", healAmount) + " health.");

                    // AI reaction
                    if (ModConfig.get().enableAI && !isGeneratingResponse &&
                        System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {
                        generateAndSayResponse(getNameForContext() +
                            " just used a golden apple to heal " + getNameForContext() +
                            "'s owner. Express concern and care.");
                    }
                    break;
                }
            }
        }
    }

    private void updateMovementState() {
        if (this.isKnockedOut) {
            // Don't update movement state when knocked out
            return;
        }

        // Check if swimming (highest priority)
        if (this.isTouchingWater() || this.isMostlySubmergedInWater()) {
            if (!this.gameContext.equals("Swimming.") &&
                !this.gameContext.equals("Swimming to follow owner.")) {
                this.gameContext = "Swimming.";
                // Optional: Add swimming event to history occasionally
                if (this.age % 200 == 0 && Math.random() < 0.1) { // ~10% chance every 10 seconds
                    String name = getNameForContext();
                    getMemory().addMessage("system", name + " is currently swimming.");
                }
            }
            return;
        }

        // Check if walking/moving
        double speedSquared = this.getVelocity().horizontalLengthSquared();
        boolean isMoving = speedSquared > 0.01; // Small threshold to account for minor movement

        if (isMoving) {
            if (!this.gameContext.equals("Walking around.") &&
                !this.gameContext.equals("Walking to follow owner.") &&
                !this.gameContext.equals("Wandering around.")) {
                this.gameContext = "Walking around.";
                // Optional: Add walking event to history occasionally
                if (this.age % 300 == 0 && Math.random() < 0.05) { // ~5% chance every 15 seconds
                    String name = getNameForContext();
                    getMemory().addMessage("system", name + " is currently walking around.");
                }
            }
        } else {
            // Not moving and not swimming
            if (!this.gameContext.equals("Standing idly.") &&
                !this.gameContext.contains("defeated") &&
                !this.gameContext.contains("fighting") &&
                !this.gameContext.contains("attacked") &&
                !this.gameContext.contains("picked up") &&
                !this.gameContext.contains("gave")) {
                this.gameContext = "Standing idly.";
            }
        }
    }

    private boolean isMostlySubmergedInWater() {
        // Check if entity is mostly submerged in water
        return this.isTouchingWater() && this.getFluidHeight(net.minecraft.registry.tag.FluidTags.WATER) > 0.9;
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
            // Add night event to history
            String name = getNameForContext();
            getMemory().addMessage("system", "Night has fallen.");
            if (!isGeneratingResponse && System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {
                generateAndSayResponse("It has just become night time. It is getting dark. Comment on this and maybe tell the player to be careful.");
            }
        } else if (!currentlyNight && this.isNight) {
            this.isNight = false;
            // Add morning event to history
            String name = getNameForContext();
            getMemory().addMessage("system", "Morning has arrived.");
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
                // Handle golden apples separately since they're not standard food items
                String itemName = stack.getName().getString();
                String name = getNameForContext();

                // Apply healing multiplier
                float healAmount = 5.0f * (float) relationshipManager.getHealingMultiplier();
                this.heal(healAmount);

                this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);

                stack.decrement(1);
                if (stack.isEmpty()) {
                    this.inventory.setStack(i, ItemStack.EMPTY);
                }

                this.lastHealTime = System.currentTimeMillis();

                // Add eating event to chat history
                getMemory().addMessage("system", name + " ate " + itemName + " from inventory to heal for " + String.format("%.1f", healAmount) + " health.");

                // Chance to react to eating (30% chance)
                if (ModConfig.get().enableAI && !isKnockedOut &&
                    Math.random() < 0.3 &&
                    !isGeneratingResponse &&
                    System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {

                    generateAndSayResponse(name + " just ate " + itemName + " from " + name + "'s inventory because " + name + " was hurt. React to healing yourself.");
                }
                return;
            }
        }
    }

    private void eatFood(ItemStack stack, int slot, FoodComponent food) {
        // Apply healing multiplier
        float healAmount = food.nutrition() * (float) relationshipManager.getHealingMultiplier();
        this.heal(healAmount);

        this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);

        // Get the item name before decrementing
        String itemName = stack.getName().getString();
        String name = getNameForContext();

        stack.decrement(1);
        if (stack.isEmpty()) {
            this.inventory.setStack(slot, ItemStack.EMPTY);
        }

        this.lastHealTime = System.currentTimeMillis();

        // NEW: Add eating event to chat history
        getMemory().addMessage("system", name + " ate " + itemName + " from inventory to heal for " + String.format("%.1f", healAmount) + " health.");

        // NEW: Chance to react to eating (30% chance)
        if (ModConfig.get().enableAI && !isKnockedOut &&
            Math.random() < 0.3 &&
            !isGeneratingResponse &&
            System.currentTimeMillis() - lastPhraseTime > SPEECH_COOLDOWN) {

            generateAndSayResponse(name + " just ate " + itemName + " from " + name + "'s inventory because " + name + " was hurt. React to healing yourself.");
        }
    }

    // --- Inventory Management Logic ---
    private void tickInventoryManagement() {
        if (!ModConfig.get().enableAI) return;
        if (System.currentTimeMillis() - lastInventoryCheckTime < INVENTORY_CHECK_COOLDOWN) return;

        if (isInventoryFull()) {
            lastInventoryCheckTime = System.currentTimeMillis();

            List<String> items = getItemNamesFromInventory();
            String name = getNameForContext();
            getMemory().addMessage("system", name + "'s inventory is full. Checking logic engine for useless items to drop...");

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

                String name = getNameForContext();
                String context = name + "'s inventory was full, so " + name + " threw away " + itemName + ".";
                generateAndSayResponse(context);
                return;
            }
        }
    }

    private void pickupNearbyItems() {
        if (isKnockedOut) return; // Can't pick up items when knocked out
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

        // Add pickup event to history
        String name = getNameForContext();
        getMemory().addMessage("system", name + " picked up items: " + itemNames + ".");
        generateAndSayResponse(name + " just picked up these items: " + itemNames + ".");
    }

    private void triggerDamageReaction(Entity attacker, boolean isSelf) {
        if (!ModConfig.get().enableAI || isKnockedOut) return;
        if (System.currentTimeMillis() - lastPhraseTime < 2000) return;

        this.lastDamageReactionTime = System.currentTimeMillis();
        String attackerName = (attacker != null) ? attacker.getName().getString() : "something";
        String name = getNameForContext();

        // Add damage event to history
        String damageEvent = isSelf
            ? name + " was attacked by " + attackerName + "."
            : "Owner was attacked by " + attackerName + ".";
        getMemory().addMessage("system", damageEvent);

        String prompt = isSelf
            ? name + " was attacked by " + attackerName + ". React with pain/anger."
            : "Your owner was attacked by " + attackerName + "! React with concern/anger.";

        generateAndSayResponse(prompt);
    }

    private void updateNameTag() {
        String displayName = this.playerCustomName.isEmpty() ? "Girlfriend" : this.playerCustomName;
        displayName += " [Lv:" + this.relationshipLevel + "]";
        if (this.isKnockedOut) displayName += " (Unconscious)";
        this.setCustomName(Text.literal(displayName));
    }

    public void feedEntity(ItemStack stack) {
        if (isKnockedOut) return; // Can't eat when knocked out
        if (stack.isOf(Items.APPLE) || stack.isOf(Items.GOLDEN_APPLE)) {
            this.heal(5.0f);
            this.addRelationship(5);
        } else {
            this.heal(2.0f);
            this.addRelationship(2);
        }
    }

    public void reactToItem(PlayerEntity player, ItemStack stack, boolean wasAddedToInventory) {
        if (!ModConfig.get().enableAI || isKnockedOut) return;
        String itemName = stack.getName().getString();
        String name = getNameForContext();

        // Add gift event to history - use consistent format
        String giftEvent = "Player gave " + name + " " + itemName + " " + (wasAddedToInventory ? "(accepted)" : "(inventory full)");
        getMemory().addMessage("system", giftEvent);

        if (isGeneratingResponse || System.currentTimeMillis() - lastPhraseTime < SPEECH_COOLDOWN) return;

        String prompt = player.getName().getString() + " gave " + name + " " + itemName + (wasAddedToInventory ? ". Accepted." : ", but inventory full.");
        generateAndSayResponse(prompt);
    }

    public void clearMemory() {
        getMemory().clear();
        if (owner != null) owner.sendMessage(Text.literal("Girlfriend memory cleared."), true);
    }

    public void setRelationshipLevel(int level) {
        this.relationshipLevel = Math.min(Math.max(0, level), this.maxRelationshipLevel);
        if (relationshipManager != null) {
            relationshipManager.setRelationshipLevel(level);
        }
    }

    public int getRelationshipLevel() {
        return this.relationshipLevel;
    }

    public void addRelationship(int amount) {
        this.relationshipLevel = Math.min(Math.max(0, this.relationshipLevel + amount), this.maxRelationshipLevel);
        if (relationshipManager != null) {
            relationshipManager.addRelationship(amount);
        }

        // Visual feedback
        if (amount > 0 && owner != null) {
            // Spawn heart particles for positive relationship
            if (getEntityWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.HEART,
                    this.getX(), this.getY() + 2, this.getZ(),
                    3, 0.3, 0.3, 0.3, 0.1);
            }
        } else if (amount < 0 && owner != null) {
            // Spawn angry particles for negative relationship
            if (getEntityWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.ANGRY_VILLAGER,
                    this.getX(), this.getY() + 2, this.getZ(),
                    3, 0.3, 0.3, 0.3, 0.1);
            }
        }
    }

    public void setPlayerCustomName(String name) { this.playerCustomName = name; }

    public void toggle() {
        if (isKnockedOut) return; // Can't toggle follow/wait when knocked out
        this.isFollowing = !this.isFollowing;
        if (this.owner != null) {
            String status = this.isFollowing ? "following you" : "waiting here";
            this.owner.sendMessage(Text.literal("♥ " + this.getName().getString() + ": i'm " + status), false);
        }
    }

    // Helper method to get the name for context
    public String getNameForContext() {
        return this.playerCustomName.isEmpty() ? "Girlfriend" : this.playerCustomName;
    }

    // Helper method to get the owner's name for context
    public String getOwnerNameForContext() {
        if (this.owner != null) {
            return this.owner.getName().getString();
        }
        return "Player";
    }

    // Helper method to heal with relationship multiplier
    public void heal(float amount) {
        amount *= (float) relationshipManager.getHealingMultiplier();
        this.setHealth(Math.min(this.getHealth() + amount, this.getMaxHealth()));
    }

    // Helper method to check if should take damage for owner
    public boolean shouldTakeDamageForOwner(float damageAmount) {
        return relationshipManager.shouldTakeDamageForOwner(damageAmount);
    }
}
