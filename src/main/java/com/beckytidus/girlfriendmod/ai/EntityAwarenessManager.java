package com.beckytidus.girlfriendmod.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

/**
 * Manages entity awareness for the girlfriend entity.
 * Scans nearby decoration entities (paintings, item frames, armor stands) and provides context for AI.
 */
public class EntityAwarenessManager {

    // Entity categories
    public enum EntityCategory {
        PAINTING("paintings", true, false),
        ITEM_FRAME("item frames", true, false),
        GLOW_ITEM_FRAME("glow item frames", true, false),
        ARMOR_STAND("armor stands", false, true);

        public final String displayName;
        public final boolean canGroup;
        public final boolean highPriority;

        EntityCategory(String displayName, boolean canGroup, boolean highPriority) {
            this.displayName = displayName;
            this.canGroup = canGroup;
            this.highPriority = highPriority;
        }
    }

    /**
     * Represents a detected decoration entity with details.
     */
    public static class DetectedDecoration {
        public final Entity entity;
        public final BlockPos pos;
        public final EntityCategory category;
        public final String displayName;
        public final String description;
        public final int distance;
        public final Direction direction;
        public final Direction facing;

        public DetectedDecoration(Entity entity, BlockPos pos, EntityCategory category, 
                                  String displayName, String description, int distance, 
                                  Direction direction, Direction facing) {
            this.entity = entity;
            this.pos = pos;
            this.category = category;
            this.displayName = displayName;
            this.description = description;
            this.distance = distance;
            this.direction = direction;
            this.facing = facing;
        }
    }

    /**
     * Result of an entity scan operation.
     */
    public static class ScanResult {
        public final Map<EntityCategory, List<DetectedDecoration>> entitiesByCategory;
        public final List<DetectedDecoration> highPriorityEntities;
        public final String summary;
        public final boolean hasChanges;
        public final Set<EntityCategory> changedCategories;

        public ScanResult(Map<EntityCategory, List<DetectedDecoration>> entitiesByCategory,
                          List<DetectedDecoration> highPriorityEntities,
                          String summary, boolean hasChanges,
                          Set<EntityCategory> changedCategories) {
            this.entitiesByCategory = entitiesByCategory;
            this.highPriorityEntities = highPriorityEntities;
            this.summary = summary;
            this.hasChanges = hasChanges;
            this.changedCategories = changedCategories;
        }

        public boolean isEmpty() {
            return entitiesByCategory.isEmpty() || entitiesByCategory.values().stream().allMatch(List::isEmpty);
        }
    }

    // Scan configuration
    private static final int DEFAULT_SCAN_RADIUS = 16;
    private static final int MAX_ENTITIES_PER_CATEGORY = 10;

    /**
     * Scan for decoration entities around a position.
     * 
     * @param world The world to scan in
     * @param center The center position to scan around
     * @param horizontalRadius Horizontal scan radius
     * @param previousResult Previous scan result for change detection (can be null)
     * @return ScanResult containing all detected decoration entities
     */
    public static ScanResult scanEntities(World world, BlockPos center,
                                          int horizontalRadius,
                                          ScanResult previousResult) {
        Map<EntityCategory, List<DetectedDecoration>> entitiesByCategory = new EnumMap<>(EntityCategory.class);
        List<DetectedDecoration> highPriorityEntities = new ArrayList<>();

        // Initialize category lists
        for (EntityCategory category : EntityCategory.values()) {
            entitiesByCategory.put(category, new ArrayList<>());
        }

        // Track counts for change detection
        Map<EntityCategory, Map<String, Integer>> currentCounts = new EnumMap<>(EntityCategory.class);
        for (EntityCategory category : EntityCategory.values()) {
            currentCounts.put(category, new HashMap<>());
        }

        // Get all entities in range - we need to check for specific types
        List<Entity> nearbyEntities = new ArrayList<>();
        
        // Add paintings
        nearbyEntities.addAll(world.getEntitiesByClass(
            PaintingEntity.class,
            new net.minecraft.util.math.Box(
                center.getX() - horizontalRadius, center.getY() - horizontalRadius/2, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + horizontalRadius/2, center.getZ() + horizontalRadius
            ),
            e -> true
        ));
        
        // Add item frames
        nearbyEntities.addAll(world.getEntitiesByClass(
            ItemFrameEntity.class,
            new net.minecraft.util.math.Box(
                center.getX() - horizontalRadius, center.getY() - horizontalRadius/2, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + horizontalRadius/2, center.getZ() + horizontalRadius
            ),
            e -> true
        ));
        
        // Add glow item frames
        nearbyEntities.addAll(world.getEntitiesByClass(
            GlowItemFrameEntity.class,
            new net.minecraft.util.math.Box(
                center.getX() - horizontalRadius, center.getY() - horizontalRadius/2, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + horizontalRadius/2, center.getZ() + horizontalRadius
            ),
            e -> true
        ));
        
        // Add armor stands
        nearbyEntities.addAll(world.getEntitiesByClass(
            ArmorStandEntity.class,
            new net.minecraft.util.math.Box(
                center.getX() - horizontalRadius, center.getY() - horizontalRadius/2, center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius, center.getY() + horizontalRadius/2, center.getZ() + horizontalRadius
            ),
            e -> true
        ));

        // Process each entity
        for (Entity entity : nearbyEntities) {
            DetectedDecoration detected = processEntity(entity, center);
            if (detected == null) continue;

            // Add to category list
            List<DetectedDecoration> categoryList = entitiesByCategory.get(detected.category);
            if (categoryList.size() < MAX_ENTITIES_PER_CATEGORY) {
                categoryList.add(detected);
            }

            // Track for high priority
            if (detected.category.highPriority) {
                highPriorityEntities.add(detected);
            }

            // Track counts for change detection
            currentCounts.get(detected.category).merge(detected.displayName, 1, Integer::sum);
        }

        // Build summary
        String summary = buildSummary(entitiesByCategory);

        // Detect changes
        Set<EntityCategory> changedCategories = new HashSet<>();
        boolean hasChanges = detectChanges(currentCounts, previousResult, changedCategories);

        return new ScanResult(entitiesByCategory, highPriorityEntities, summary, hasChanges, changedCategories);
    }

    /**
     * Scan entities with default radius.
     */
    public static ScanResult scanEntities(World world, BlockPos center, ScanResult previousResult) {
        return scanEntities(world, center, DEFAULT_SCAN_RADIUS, previousResult);
    }

    /**
     * Process an entity and create a DetectedDecoration.
     */
    private static DetectedDecoration processEntity(Entity entity, BlockPos center) {
        BlockPos entityPos = entity.getBlockPos();
        int dx = entityPos.getX() - center.getX();
        int dy = entityPos.getY() - center.getY();
        int dz = entityPos.getZ() - center.getZ();
        int distance = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
        Direction direction = getDirection(dx, dz);

        if (entity instanceof PaintingEntity painting) {
            return processPainting(painting, entityPos, distance, direction);
        } else if (entity instanceof GlowItemFrameEntity glowItemFrame) {
            return processGlowItemFrame(glowItemFrame, entityPos, distance, direction);
        } else if (entity instanceof ItemFrameEntity itemFrame) {
            return processItemFrame(itemFrame, entityPos, distance, direction);
        } else if (entity instanceof ArmorStandEntity armorStand) {
            return processArmorStand(armorStand, entityPos, distance, direction);
        }

        return null;
    }

    /**
     * Process a painting entity.
     */
    private static DetectedDecoration processPainting(PaintingEntity painting, BlockPos pos, 
                                                       int distance, Direction direction) {
        String artName = getPaintingArtName(painting);
        Direction facing = painting.getHorizontalFacing();
        String description = "A painting depicting \"" + artName + "\"";
        
        return new DetectedDecoration(
            painting, pos, EntityCategory.PAINTING, 
            "Painting (" + artName + ")", description, 
            distance, direction, facing
        );
    }

    /**
     * Get the art/motive name for a painting.
     */
    private static String getPaintingArtName(PaintingEntity painting) {
        try {
            RegistryEntry<PaintingVariant> variant = painting.getVariant();
            if (variant != null) {
                Optional<RegistryKey<PaintingVariant>> key = variant.getKey();
                if (key.isPresent()) {
                    String id = key.get().getValue().toString();
                    // Convert "minecraft:alban" to "Alban"
                    String name = id.replace("minecraft:", "");
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
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return "Unknown Art";
    }

    /**
     * Process an item frame entity.
     */
    private static DetectedDecoration processItemFrame(ItemFrameEntity itemFrame, BlockPos pos,
                                                        int distance, Direction direction) {
        ItemStack heldItem = itemFrame.getHeldItemStack();
        Direction facing = itemFrame.getHorizontalFacing();
        
        String itemName;
        String description;
        
        if (heldItem.isEmpty()) {
            itemName = "Empty Item Frame";
            description = "An empty item frame on the wall";
        } else {
            itemName = "Item Frame with " + heldItem.getName().getString();
            int rotation = itemFrame.getRotation();
            description = "An item frame containing " + heldItem.getName().getString() + 
                         (heldItem.getCount() > 1 ? " (x" + heldItem.getCount() + ")" : "") +
                         ", rotated " + (rotation * 45) + " degrees";
        }
        
        return new DetectedDecoration(
            itemFrame, pos, EntityCategory.ITEM_FRAME,
            itemName, description, distance, direction, facing
        );
    }

    /**
     * Process a glow item frame entity.
     */
    private static DetectedDecoration processGlowItemFrame(GlowItemFrameEntity glowItemFrame, BlockPos pos,
                                                            int distance, Direction direction) {
        ItemStack heldItem = glowItemFrame.getHeldItemStack();
        Direction facing = glowItemFrame.getHorizontalFacing();
        
        String itemName;
        String description;
        
        if (heldItem.isEmpty()) {
            itemName = "Empty Glow Item Frame";
            description = "An empty glowing item frame on the wall";
        } else {
            itemName = "Glow Item Frame with " + heldItem.getName().getString();
            int rotation = glowItemFrame.getRotation();
            description = "A glowing item frame containing " + heldItem.getName().getString() + 
                         (heldItem.getCount() > 1 ? " (x" + heldItem.getCount() + ")" : "") +
                         " (illuminated), rotated " + (rotation * 45) + " degrees";
        }
        
        return new DetectedDecoration(
            glowItemFrame, pos, EntityCategory.GLOW_ITEM_FRAME,
            itemName, description, distance, direction, facing
        );
    }

    /**
     * Process an armor stand entity.
     */
    private static DetectedDecoration processArmorStand(ArmorStandEntity armorStand, BlockPos pos,
                                                         int distance, Direction direction) {
        StringBuilder description = new StringBuilder();
        List<String> equipment = new ArrayList<>();
        
        // Check each equipment slot
        checkEquipment(armorStand, EquipmentSlot.HEAD, "Head", equipment);
        checkEquipment(armorStand, EquipmentSlot.CHEST, "Chest", equipment);
        checkEquipment(armorStand, EquipmentSlot.LEGS, "Legs", equipment);
        checkEquipment(armorStand, EquipmentSlot.FEET, "Feet", equipment);
        checkEquipment(armorStand, EquipmentSlot.MAINHAND, "Main Hand", equipment);
        checkEquipment(armorStand, EquipmentSlot.OFFHAND, "Off Hand", equipment);
        
        String displayName;
        if (equipment.isEmpty()) {
            displayName = "Empty Armor Stand";
            description.append("An empty armor stand");
        } else {
            displayName = "Armor Stand (" + equipment.size() + " items)";
            description.append("An armor stand wearing: ");
            description.append(String.join(", ", equipment));
        }
        
        // Add pose information
        if (armorStand.shouldShowArms()) {
            description.append(" with arms visible");
        }
        // Note: Armor stands in modern Minecraft don't have a direct hasNoBasePlate method
        // The base plate visibility is handled differently
        if (armorStand.isSmall()) {
            description.insert(0, "A small ");
        }
        
        return new DetectedDecoration(
            armorStand, pos, EntityCategory.ARMOR_STAND,
            displayName, description.toString(), distance, direction, null
        );
    }

    /**
     * Check equipment in a specific slot and add to the list.
     */
    private static void checkEquipment(ArmorStandEntity armorStand, EquipmentSlot slot, 
                                        String slotName, List<String> equipment) {
        ItemStack stack = armorStand.getEquippedStack(slot);
        if (!stack.isEmpty()) {
            equipment.add(stack.getName().getString() + " (" + slotName + ")");
        }
    }

    /**
     * Get the cardinal direction from dx, dz offsets.
     */
    private static Direction getDirection(int dx, int dz) {
        if (dx == 0 && dz == 0) return null;
        
        double angle = Math.atan2(dz, dx) * 180 / Math.PI;
        
        if (angle >= -22.5 && angle < 22.5) return Direction.EAST;
        if (angle >= 22.5 && angle < 67.5) return Direction.SOUTH;
        if (angle >= 67.5 && angle < 112.5) return Direction.SOUTH;
        if (angle >= 112.5 && angle < 157.5) return Direction.WEST;
        if (angle >= 157.5 || angle < -157.5) return Direction.WEST;
        if (angle >= -157.5 && angle < -112.5) return Direction.NORTH;
        if (angle >= -112.5 && angle < -67.5) return Direction.NORTH;
        if (angle >= -67.5 && angle < -22.5) return Direction.EAST;
        
        return Direction.NORTH;
    }

    /**
     * Build a human-readable summary of detected entities.
     */
    private static String buildSummary(Map<EntityCategory, List<DetectedDecoration>> entitiesByCategory) {
        StringBuilder summary = new StringBuilder();
        boolean first = true;

        for (EntityCategory category : EntityCategory.values()) {
            List<DetectedDecoration> entities = entitiesByCategory.get(category);
            if (entities.isEmpty()) continue;

            if (!first) summary.append(". ");
            first = false;

            summary.append(category.displayName).append(": ");

            if (category.canGroup) {
                // Group by display name
                Map<String, Integer> counts = new HashMap<>();
                for (DetectedDecoration dd : entities) {
                    counts.merge(dd.displayName, 1, Integer::sum);
                }
                
                summary.append(counts.entrySet().stream()
                    .map(e -> e.getValue() > 1 ? e.getValue() + "x " + e.getKey() : e.getKey())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            } else {
                // List individually with details
                int limit = Math.min(3, entities.size());
                summary.append(entities.subList(0, limit).stream()
                    .map(dd -> dd.displayName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
                if (entities.size() > limit) {
                    summary.append(" and ").append(entities.size() - limit).append(" more");
                }
            }
        }

        return summary.toString();
    }

    /**
     * Detect changes between current and previous scan.
     */
    private static boolean detectChanges(Map<EntityCategory, Map<String, Integer>> currentCounts,
                                         ScanResult previousResult,
                                         Set<EntityCategory> changedCategories) {
        if (previousResult == null) {
            return !currentCounts.values().stream().allMatch(Map::isEmpty);
        }

        // Compare counts for each category
        for (EntityCategory category : EntityCategory.values()) {
            Map<String, Integer> current = currentCounts.get(category);
            List<DetectedDecoration> previous = previousResult.entitiesByCategory.get(category);

            // Count previous entities
            Map<String, Integer> previousCounts = new HashMap<>();
            for (DetectedDecoration dd : previous) {
                previousCounts.merge(dd.displayName, 1, Integer::sum);
            }

            // Compare
            if (!current.equals(previousCounts)) {
                changedCategories.add(category);
            }
        }

        return !changedCategories.isEmpty();
    }

    /**
     * Get a formatted string describing direction and distance for an entity.
     */
    public static String formatLocation(DetectedDecoration decoration) {
        if (decoration.direction == null) {
            return decoration.distance + " blocks away";
        }
        return decoration.distance + " blocks " + formatDirection(decoration.direction);
    }

    /**
     * Format a direction for display.
     */
    private static String formatDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> "North";
            case SOUTH -> "South";
            case EAST -> "East";
            case WEST -> "West";
            case UP -> "Above";
            case DOWN -> "Below";
            default -> dir.toString();
        };
    }

    /**
     * Generate a prompt for AI reaction based on detected entities.
     */
    public static String generateReactionPrompt(List<DetectedDecoration> newEntities, String girlfriendName) {
        if (newEntities.isEmpty()) return null;

        // Find the most interesting entity to react to
        DetectedDecoration mostInteresting = null;
        for (DetectedDecoration dd : newEntities) {
            // Armor stands are high priority
            if (dd.category == EntityCategory.ARMOR_STAND) {
                mostInteresting = dd;
                break;
            }
            // Then glow item frames
            if (dd.category == EntityCategory.GLOW_ITEM_FRAME) {
                mostInteresting = dd;
            }
            // Then regular item frames with items
            if (dd.category == EntityCategory.ITEM_FRAME && 
                !dd.displayName.startsWith("Empty")) {
                if (mostInteresting == null || mostInteresting.category == EntityCategory.PAINTING) {
                    mostInteresting = dd;
                }
            }
        }

        // Default to closest entity
        if (mostInteresting == null) {
            mostInteresting = newEntities.stream()
                .min(Comparator.comparingInt(dd -> dd.distance))
                .orElse(null);
        }

        if (mostInteresting == null) return null;

        String location = formatLocation(mostInteresting);
        
        return switch (mostInteresting.category) {
            case PAINTING -> girlfriendName + " noticed a " + mostInteresting.displayName + 
                " " + location + ". React with appreciation for the art.";
            case ITEM_FRAME -> {
                if (mostInteresting.displayName.startsWith("Empty")) {
                    yield girlfriendName + " noticed an empty item frame " + location + 
                        ". Briefly acknowledge it.";
                }
                yield girlfriendName + " noticed " + mostInteresting.description + " " + location + 
                    ". Comment on the displayed item.";
            }
            case GLOW_ITEM_FRAME -> girlfriendName + " noticed " + mostInteresting.description + 
                " " + location + ". Comment on the glowing display.";
            case ARMOR_STAND -> girlfriendName + " noticed " + mostInteresting.description + 
                " " + location + ". Comment on the armor stand and its equipment.";
            default -> girlfriendName + " noticed " + mostInteresting.displayName + 
                " " + location + ". Briefly acknowledge it.";
        };
    }
}