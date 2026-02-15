package com.beckytidus.girlfriendmod.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

/**
 * Manages block awareness for the girlfriend entity.
 * Scans nearby blocks and categorizes them into meaningful groups for AI context.
 */
public class BlockAwarenessManager {

    // Block categories with their associated blocks
    public enum BlockCategory {
        FLOWERS("flowers", true, false),
        ORES("ores", true, true),
        RARE_ORES("rare ores", true, true),
        STORAGE("storage", true, false),
        PROCESSING("processing", false, false),
        REDSTONE("redstone", false, false),
        SPECIAL("special blocks", false, true),
        DANGEROUS("dangerous blocks", false, true),
        WATER("water", true, false),
        LAVA("lava", true, true),
        PORTAL("portal", false, true),
        VILLAGE_STRUCTURES("village structures", false, false);

        public final String displayName;
        public final boolean canGroup; // Can show as "3x Flowers"
        public final boolean highPriority; // Should trigger reactions

        BlockCategory(String displayName, boolean canGroup, boolean highPriority) {
            this.displayName = displayName;
            this.canGroup = canGroup;
            this.highPriority = highPriority;
        }
    }

    /**
     * Represents a detected block with its position relative to an origin point.
     */
    public static class DetectedBlock {
        public final Block block;
        public final BlockPos pos;
        public final BlockCategory category;
        public final String displayName;
        public final int distance;
        public final Direction direction;

        public DetectedBlock(Block block, BlockPos pos, BlockCategory category, String displayName, int distance, Direction direction) {
            this.block = block;
            this.pos = pos;
            this.category = category;
            this.displayName = displayName;
            this.distance = distance;
            this.direction = direction;
        }
    }

    /**
     * Result of a block scan operation.
     */
    public static class ScanResult {
        public final Map<BlockCategory, List<DetectedBlock>> blocksByCategory;
        public final List<DetectedBlock> highPriorityBlocks;
        public final String summary;
        public final boolean hasChanges;
        public final Set<BlockCategory> changedCategories;

        public ScanResult(Map<BlockCategory, List<DetectedBlock>> blocksByCategory, 
                          List<DetectedBlock> highPriorityBlocks,
                          String summary, 
                          boolean hasChanges,
                          Set<BlockCategory> changedCategories) {
            this.blocksByCategory = blocksByCategory;
            this.highPriorityBlocks = highPriorityBlocks;
            this.summary = summary;
            this.hasChanges = hasChanges;
            this.changedCategories = changedCategories;
        }

        public boolean isEmpty() {
            return blocksByCategory.isEmpty() || blocksByCategory.values().stream().allMatch(List::isEmpty);
        }
    }

    // Scan configuration
    private static final int DEFAULT_SCAN_RADIUS = 16;
    private static final int DEFAULT_VERTICAL_RADIUS = 8;
    private static final int MAX_BLOCKS_PER_CATEGORY = 10; // Limit for context

    // Category mappings - initialized statically for performance
    private static final Map<Block, BlockCategory> BLOCK_CATEGORIES = new HashMap<>();
    private static final Map<Block, String> CUSTOM_NAMES = new HashMap<>();
    private static final Set<Block> HIGH_VALUE_ORES = new HashSet<>();

    static {
        initializeCategoryMappings();
    }

    private static void initializeCategoryMappings() {
        // === FLOWERS ===
        // Small flowers
        addBlock(Blocks.DANDELION, BlockCategory.FLOWERS, "Dandelion");
        addBlock(Blocks.POPPY, BlockCategory.FLOWERS, "Poppy");
        addBlock(Blocks.BLUE_ORCHID, BlockCategory.FLOWERS, "Blue Orchid");
        addBlock(Blocks.ALLIUM, BlockCategory.FLOWERS, "Allium");
        addBlock(Blocks.AZURE_BLUET, BlockCategory.FLOWERS, "Azure Bluet");
        addBlock(Blocks.RED_TULIP, BlockCategory.FLOWERS, "Red Tulip");
        addBlock(Blocks.ORANGE_TULIP, BlockCategory.FLOWERS, "Orange Tulip");
        addBlock(Blocks.WHITE_TULIP, BlockCategory.FLOWERS, "White Tulip");
        addBlock(Blocks.PINK_TULIP, BlockCategory.FLOWERS, "Pink Tulip");
        addBlock(Blocks.OXEYE_DAISY, BlockCategory.FLOWERS, "Oxeye Daisy");
        addBlock(Blocks.CORNFLOWER, BlockCategory.FLOWERS, "Cornflower");
        addBlock(Blocks.LILY_OF_THE_VALLEY, BlockCategory.FLOWERS, "Lily of the Valley");
        addBlock(Blocks.WITHER_ROSE, BlockCategory.FLOWERS, "Wither Rose");
        addBlock(Blocks.TORCHFLOWER, BlockCategory.FLOWERS, "Torchflower");
        addBlock(Blocks.OPEN_EYEBLOSSOM, BlockCategory.FLOWERS, "Open Eyeblossom");
        addBlock(Blocks.CLOSED_EYEBLOSSOM, BlockCategory.FLOWERS, "Closed Eyeblossom");
        
        // Tall flowers
        addBlock(Blocks.SUNFLOWER, BlockCategory.FLOWERS, "Sunflower");
        addBlock(Blocks.LILAC, BlockCategory.FLOWERS, "Lilac");
        addBlock(Blocks.PEONY, BlockCategory.FLOWERS, "Peony");
        addBlock(Blocks.ROSE_BUSH, BlockCategory.FLOWERS, "Rose Bush");
        addBlock(Blocks.PITCHER_PLANT, BlockCategory.FLOWERS, "Pitcher Plant");
        
        // Flower-related blocks
        addBlock(Blocks.FLOWERING_AZALEA, BlockCategory.FLOWERS, "Flowering Azalea");
        addBlock(Blocks.FLOWERING_AZALEA_LEAVES, BlockCategory.FLOWERS, "Flowering Azalea Leaves");
        addBlock(Blocks.PINK_PETALS, BlockCategory.FLOWERS, "Pink Petals");
        addBlock(Blocks.CHERRY_LEAVES, BlockCategory.FLOWERS, "Cherry Leaves");
        addBlock(Blocks.SPORE_BLOSSOM, BlockCategory.FLOWERS, "Spore Blossom");
        addBlock(Blocks.CHORUS_FLOWER, BlockCategory.FLOWERS, "Chorus Flower");
        addBlock(Blocks.CACTUS_FLOWER, BlockCategory.FLOWERS, "Cactus Flower");
        addBlock(Blocks.WILDFLOWERS, BlockCategory.FLOWERS, "Wildflowers");

        // === ORES (Common) ===
        addBlock(Blocks.COAL_ORE, BlockCategory.ORES, "Coal Ore");
        addBlock(Blocks.DEEPSLATE_COAL_ORE, BlockCategory.ORES, "Deepslate Coal Ore");
        addBlock(Blocks.IRON_ORE, BlockCategory.ORES, "Iron Ore");
        addBlock(Blocks.DEEPSLATE_IRON_ORE, BlockCategory.ORES, "Deepslate Iron Ore");
        addBlock(Blocks.COPPER_ORE, BlockCategory.ORES, "Copper Ore");
        addBlock(Blocks.DEEPSLATE_COPPER_ORE, BlockCategory.ORES, "Deepslate Copper Ore");
        
        // === RARE ORES (High value - trigger reactions) ===
        addBlock(Blocks.GOLD_ORE, BlockCategory.RARE_ORES, "Gold Ore");
        addBlock(Blocks.DEEPSLATE_GOLD_ORE, BlockCategory.RARE_ORES, "Deepslate Gold Ore");
        addBlock(Blocks.REDSTONE_ORE, BlockCategory.RARE_ORES, "Redstone Ore");
        addBlock(Blocks.DEEPSLATE_REDSTONE_ORE, BlockCategory.RARE_ORES, "Deepslate Redstone Ore");
        addBlock(Blocks.LAPIS_ORE, BlockCategory.RARE_ORES, "Lapis Ore");
        addBlock(Blocks.DEEPSLATE_LAPIS_ORE, BlockCategory.RARE_ORES, "Deepslate Lapis Ore");
        addBlock(Blocks.EMERALD_ORE, BlockCategory.RARE_ORES, "Emerald Ore");
        addBlock(Blocks.DEEPSLATE_EMERALD_ORE, BlockCategory.RARE_ORES, "Deepslate Emerald Ore");
        addBlock(Blocks.DIAMOND_ORE, BlockCategory.RARE_ORES, "Diamond Ore");
        addBlock(Blocks.DEEPSLATE_DIAMOND_ORE, BlockCategory.RARE_ORES, "Deepslate Diamond Ore");
        addBlock(Blocks.NETHER_GOLD_ORE, BlockCategory.RARE_ORES, "Nether Gold Ore");
        addBlock(Blocks.NETHER_QUARTZ_ORE, BlockCategory.RARE_ORES, "Nether Quartz Ore");
        addBlock(Blocks.ANCIENT_DEBRIS, BlockCategory.RARE_ORES, "Ancient Debris");
        
        // Track high-value ores separately for reactions
        HIGH_VALUE_ORES.add(Blocks.DIAMOND_ORE);
        HIGH_VALUE_ORES.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        HIGH_VALUE_ORES.add(Blocks.EMERALD_ORE);
        HIGH_VALUE_ORES.add(Blocks.DEEPSLATE_EMERALD_ORE);
        HIGH_VALUE_ORES.add(Blocks.ANCIENT_DEBRIS);

        // === STORAGE ===
        addBlock(Blocks.CHEST, BlockCategory.STORAGE, "Chest");
        addBlock(Blocks.TRAPPED_CHEST, BlockCategory.STORAGE, "Trapped Chest");
        addBlock(Blocks.BARREL, BlockCategory.STORAGE, "Barrel");
        addBlock(Blocks.ENDER_CHEST, BlockCategory.STORAGE, "Ender Chest");
        addBlock(Blocks.SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.WHITE_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.ORANGE_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.MAGENTA_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.LIGHT_BLUE_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.YELLOW_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.LIME_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.PINK_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.GRAY_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.LIGHT_GRAY_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.CYAN_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.PURPLE_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.BLUE_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.BROWN_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.GREEN_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.RED_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");
        addBlock(Blocks.BLACK_SHULKER_BOX, BlockCategory.STORAGE, "Shulker Box");

        // === PROCESSING (Furnaces, etc.) ===
        addBlock(Blocks.FURNACE, BlockCategory.PROCESSING, "Furnace");
        addBlock(Blocks.BLAST_FURNACE, BlockCategory.PROCESSING, "Blast Furnace");
        addBlock(Blocks.SMOKER, BlockCategory.PROCESSING, "Smoker");
        addBlock(Blocks.CRAFTING_TABLE, BlockCategory.PROCESSING, "Crafting Table");
        addBlock(Blocks.SMITHING_TABLE, BlockCategory.PROCESSING, "Smithing Table");
        addBlock(Blocks.GRINDSTONE, BlockCategory.PROCESSING, "Grindstone");
        addBlock(Blocks.STONECUTTER, BlockCategory.PROCESSING, "Stonecutter");
        addBlock(Blocks.BREWING_STAND, BlockCategory.PROCESSING, "Brewing Stand");
        addBlock(Blocks.CAMPFIRE, BlockCategory.PROCESSING, "Campfire");
        addBlock(Blocks.SOUL_CAMPFIRE, BlockCategory.PROCESSING, "Soul Campfire");

        // === REDSTONE ===
        addBlock(Blocks.REDSTONE_BLOCK, BlockCategory.REDSTONE, "Redstone Block");
        addBlock(Blocks.REDSTONE_WIRE, BlockCategory.REDSTONE, "Redstone Dust");
        addBlock(Blocks.REDSTONE_TORCH, BlockCategory.REDSTONE, "Redstone Torch");
        addBlock(Blocks.REDSTONE_WALL_TORCH, BlockCategory.REDSTONE, "Redstone Torch");
        addBlock(Blocks.REPEATER, BlockCategory.REDSTONE, "Repeater");
        addBlock(Blocks.COMPARATOR, BlockCategory.REDSTONE, "Comparator");
        addBlock(Blocks.PISTON, BlockCategory.REDSTONE, "Piston");
        addBlock(Blocks.STICKY_PISTON, BlockCategory.REDSTONE, "Sticky Piston");
        addBlock(Blocks.OBSERVER, BlockCategory.REDSTONE, "Observer");
        addBlock(Blocks.DISPENSER, BlockCategory.REDSTONE, "Dispenser");
        addBlock(Blocks.DROPPER, BlockCategory.REDSTONE, "Dropper");
        addBlock(Blocks.HOPPER, BlockCategory.REDSTONE, "Hopper");
        addBlock(Blocks.LEVER, BlockCategory.REDSTONE, "Lever");
        addBlock(Blocks.STONE_BUTTON, BlockCategory.REDSTONE, "Button");
        addBlock(Blocks.OAK_BUTTON, BlockCategory.REDSTONE, "Button");
        addBlock(Blocks.TRIPWIRE_HOOK, BlockCategory.REDSTONE, "Tripwire Hook");
        addBlock(Blocks.DAYLIGHT_DETECTOR, BlockCategory.REDSTONE, "Daylight Detector");
        addBlock(Blocks.TARGET, BlockCategory.REDSTONE, "Target");
        addBlock(Blocks.NOTE_BLOCK, BlockCategory.REDSTONE, "Note Block");
        addBlock(Blocks.SCULK_SENSOR, BlockCategory.REDSTONE, "Sculk Sensor");
        addBlock(Blocks.CALIBRATED_SCULK_SENSOR, BlockCategory.REDSTONE, "Calibrated Sculk Sensor");
        addBlock(Blocks.SCULK_SHRIEKER, BlockCategory.REDSTONE, "Sculk Shrieker");

        // === SPECIAL ===
        addBlock(Blocks.SPAWNER, BlockCategory.SPECIAL, "Spawner");
        addBlock(Blocks.BEACON, BlockCategory.SPECIAL, "Beacon");
        addBlock(Blocks.ENCHANTING_TABLE, BlockCategory.SPECIAL, "Enchanting Table");
        addBlock(Blocks.ANVIL, BlockCategory.SPECIAL, "Anvil");
        addBlock(Blocks.CHIPPED_ANVIL, BlockCategory.SPECIAL, "Chipped Anvil");
        addBlock(Blocks.DAMAGED_ANVIL, BlockCategory.SPECIAL, "Damaged Anvil");
        addBlock(Blocks.END_PORTAL_FRAME, BlockCategory.SPECIAL, "End Portal Frame");
        addBlock(Blocks.END_PORTAL, BlockCategory.SPECIAL, "End Portal");
        addBlock(Blocks.END_GATEWAY, BlockCategory.SPECIAL, "End Gateway");
        addBlock(Blocks.RESPAWN_ANCHOR, BlockCategory.SPECIAL, "Respawn Anchor");
        addBlock(Blocks.CONDUIT, BlockCategory.SPECIAL, "Conduit");
        addBlock(Blocks.TRIAL_SPAWNER, BlockCategory.SPECIAL, "Trial Spawner");
        addBlock(Blocks.VAULT, BlockCategory.SPECIAL, "Vault");
        addBlock(Blocks.CREAKING_HEART, BlockCategory.SPECIAL, "Creaking Heart");

        // === DANGEROUS ===
        addBlock(Blocks.LAVA, BlockCategory.LAVA, "Lava");
        addBlock(Blocks.FIRE, BlockCategory.DANGEROUS, "Fire");
        addBlock(Blocks.SOUL_FIRE, BlockCategory.DANGEROUS, "Soul Fire");
        addBlock(Blocks.MAGMA_BLOCK, BlockCategory.DANGEROUS, "Magma Block");
        addBlock(Blocks.SWEET_BERRY_BUSH, BlockCategory.DANGEROUS, "Sweet Berry Bush");
        addBlock(Blocks.CACTUS, BlockCategory.DANGEROUS, "Cactus");
        addBlock(Blocks.WITHER_ROSE, BlockCategory.DANGEROUS, "Wither Rose");
        addBlock(Blocks.POWDER_SNOW, BlockCategory.DANGEROUS, "Powder Snow");
        addBlock(Blocks.POINTED_DRIPSTONE, BlockCategory.DANGEROUS, "Pointed Dripstone");

        // === WATER ===
        addBlock(Blocks.WATER, BlockCategory.WATER, "Water");
        
        // === PORTAL ===
        addBlock(Blocks.NETHER_PORTAL, BlockCategory.PORTAL, "Nether Portal");
        
        // === VILLAGE STRUCTURES ===
        addBlock(Blocks.BELL, BlockCategory.VILLAGE_STRUCTURES, "Bell");
        addBlock(Blocks.LECTERN, BlockCategory.VILLAGE_STRUCTURES, "Lectern");
        addBlock(Blocks.FLETCHING_TABLE, BlockCategory.VILLAGE_STRUCTURES, "Fletching Table");
        addBlock(Blocks.CARTOGRAPHY_TABLE, BlockCategory.VILLAGE_STRUCTURES, "Cartography Table");
        addBlock(Blocks.LOOM, BlockCategory.VILLAGE_STRUCTURES, "Loom");
        addBlock(Blocks.BLAST_FURNACE, BlockCategory.VILLAGE_STRUCTURES, "Blast Furnace");
        addBlock(Blocks.SMOKER, BlockCategory.VILLAGE_STRUCTURES, "Smoker");
    }

    private static void addBlock(Block block, BlockCategory category, String displayName) {
        BLOCK_CATEGORIES.put(block, category);
        CUSTOM_NAMES.put(block, displayName);
    }

    /**
     * Get the category for a block.
     */
    public static BlockCategory getCategory(Block block) {
        return BLOCK_CATEGORIES.get(block);
    }

    /**
     * Get the display name for a block.
     */
    public static String getDisplayName(Block block) {
        return CUSTOM_NAMES.getOrDefault(block, block.getName().getString());
    }

    /**
     * Check if a block is a high-value ore (diamond, emerald, ancient debris).
     */
    public static boolean isHighValueOre(Block block) {
        return HIGH_VALUE_ORES.contains(block);
    }

    /**
     * Scan blocks around a position.
     * 
     * @param world The world to scan in
     * @param center The center position to scan around
     * @param horizontalRadius Horizontal scan radius
     * @param verticalRadius Vertical scan radius
     * @param previousResult Previous scan result for change detection (can be null)
     * @return ScanResult containing all detected blocks
     */
    public static ScanResult scanBlocks(World world, BlockPos center, 
                                        int horizontalRadius, int verticalRadius,
                                        ScanResult previousResult) {
        Map<BlockCategory, List<DetectedBlock>> blocksByCategory = new EnumMap<>(BlockCategory.class);
        List<DetectedBlock> highPriorityBlocks = new ArrayList<>();
        
        // Initialize category lists
        for (BlockCategory category : BlockCategory.values()) {
            blocksByCategory.put(category, new ArrayList<>());
        }

        // Track counts for change detection
        Map<BlockCategory, Map<Block, Integer>> currentCounts = new EnumMap<>(BlockCategory.class);
        for (BlockCategory category : BlockCategory.values()) {
            currentCounts.put(category, new HashMap<>());
        }

        // Scan in a sphere around the center
        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                    // Skip if outside sphere
                    if (dx * dx + dy * dy + dz * dz > horizontalRadius * horizontalRadius + verticalRadius * verticalRadius) {
                        continue;
                    }

                    BlockPos checkPos = center.add(dx, dy, dz);
                    BlockState state = world.getBlockState(checkPos);
                    Block block = state.getBlock();

                    BlockCategory category = BLOCK_CATEGORIES.get(block);
                    if (category == null) continue;

                    // Skip water/lava if it's just a flowing variant (not source)
                    if (block == Blocks.WATER || block == Blocks.LAVA) {
                        // Only count source blocks
                        if (state.getFluidState().getLevel() != 8) continue;
                    }

                    int distance = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    Direction direction = getDirection(dx, dz);
                    String displayName = getDisplayName(block);

                    DetectedBlock detected = new DetectedBlock(block, checkPos, category, displayName, distance, direction);

                    // Add to category list (limit per category)
                    List<DetectedBlock> categoryList = blocksByCategory.get(category);
                    if (categoryList.size() < MAX_BLOCKS_PER_CATEGORY) {
                        categoryList.add(detected);
                    }

                    // Track for high priority
                    if (category.highPriority) {
                        highPriorityBlocks.add(detected);
                    }

                    // Track counts for change detection
                    currentCounts.get(category).merge(block, 1, Integer::sum);
                }
            }
        }

        // Build summary
        String summary = buildSummary(blocksByCategory);

        // Detect changes
        Set<BlockCategory> changedCategories = new HashSet<>();
        boolean hasChanges = detectChanges(currentCounts, previousResult, changedCategories);

        return new ScanResult(blocksByCategory, highPriorityBlocks, summary, hasChanges, changedCategories);
    }

    /**
     * Scan blocks with default radius.
     */
    public static ScanResult scanBlocks(World world, BlockPos center, ScanResult previousResult) {
        return scanBlocks(world, center, DEFAULT_SCAN_RADIUS, DEFAULT_VERTICAL_RADIUS, previousResult);
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
     * Build a human-readable summary of detected blocks.
     */
    private static String buildSummary(Map<BlockCategory, List<DetectedBlock>> blocksByCategory) {
        StringBuilder summary = new StringBuilder();
        boolean first = true;

        for (BlockCategory category : BlockCategory.values()) {
            List<DetectedBlock> blocks = blocksByCategory.get(category);
            if (blocks.isEmpty()) continue;

            if (!first) summary.append(". ");
            first = false;

            summary.append(category.displayName).append(": ");

            if (category.canGroup) {
                // Group by block type
                Map<Block, Integer> counts = new HashMap<>();
                for (DetectedBlock db : blocks) {
                    counts.merge(db.block, 1, Integer::sum);
                }
                
                summary.append(counts.entrySet().stream()
                    .map(e -> e.getValue() > 1 ? e.getValue() + "x " + getDisplayName(e.getKey()) : getDisplayName(e.getKey()))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            } else {
                // List individually but limited
                int limit = Math.min(3, blocks.size());
                summary.append(blocks.subList(0, limit).stream()
                    .map(db -> db.displayName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
                if (blocks.size() > limit) {
                    summary.append(" and ").append(blocks.size() - limit).append(" more");
                }
            }
        }

        return summary.toString();
    }

    /**
     * Detect changes between current and previous scan.
     */
    private static boolean detectChanges(Map<BlockCategory, Map<Block, Integer>> currentCounts,
                                         ScanResult previousResult,
                                         Set<BlockCategory> changedCategories) {
        if (previousResult == null) {
            return !currentCounts.values().stream().allMatch(Map::isEmpty);
        }

        // Compare counts for each category
        for (BlockCategory category : BlockCategory.values()) {
            Map<Block, Integer> current = currentCounts.get(category);
            List<DetectedBlock> previous = previousResult.blocksByCategory.get(category);

            // Count previous blocks
            Map<Block, Integer> previousCounts = new HashMap<>();
            for (DetectedBlock db : previous) {
                previousCounts.merge(db.block, 1, Integer::sum);
            }

            // Compare
            if (!current.equals(previousCounts)) {
                changedCategories.add(category);
            }
        }

        return !changedCategories.isEmpty();
    }

    /**
     * Get a formatted string describing direction and distance for a block.
     */
    public static String formatLocation(DetectedBlock block) {
        if (block.direction == null) {
            return block.distance + " blocks away";
        }
        return block.distance + " blocks " + formatDirection(block.direction);
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
     * Generate a prompt for AI reaction based on detected blocks.
     */
    public static String generateReactionPrompt(List<DetectedBlock> newBlocks, String girlfriendName) {
        if (newBlocks.isEmpty()) return null;

        // Find the most interesting block(s) to react to
        DetectedBlock mostInteresting = null;
        for (DetectedBlock db : newBlocks) {
            if (db.category == BlockCategory.RARE_ORES) {
                mostInteresting = db;
                break;
            }
            if (db.category.highPriority && (mostInteresting == null || db.distance < mostInteresting.distance)) {
                mostInteresting = db;
            }
        }

        if (mostInteresting == null) {
            // Pick closest high-priority or first block
            mostInteresting = newBlocks.stream()
                .min(Comparator.comparingInt(db -> db.distance))
                .orElse(null);
        }

        if (mostInteresting == null) return null;

        String location = formatLocation(mostInteresting);
        
        return switch (mostInteresting.category) {
            case RARE_ORES -> {
                if (isHighValueOre(mostInteresting.block)) {
                    yield girlfriendName + " just spotted " + mostInteresting.displayName + " " + location + "! React with excitement - this is a rare and valuable find!";
                }
                yield girlfriendName + " found " + mostInteresting.displayName + " " + location + ". Point it out helpfully.";
            }
            case FLOWERS -> girlfriendName + " noticed some beautiful " + mostInteresting.displayName + "s " + location + ". React with appreciation for the pretty flowers.";
            case STORAGE -> girlfriendName + " sees a " + mostInteresting.displayName + " " + location + ". Show mild curiosity about what might be inside.";
            case SPECIAL -> girlfriendName + " discovered a " + mostInteresting.displayName + " " + location + "! React with excitement or awe at this special find.";
            case LAVA, DANGEROUS -> girlfriendName + " noticed " + mostInteresting.displayName + " " + location + ". Warn the player to be careful!";
            case PORTAL -> girlfriendName + " sees a " + mostInteresting.displayName + " " + location + ". React with wonder or curiosity.";
            case WATER -> null; // Don't react to water
            default -> girlfriendName + " noticed " + mostInteresting.displayName + " " + location + ". Briefly acknowledge it.";
        };
    }
}