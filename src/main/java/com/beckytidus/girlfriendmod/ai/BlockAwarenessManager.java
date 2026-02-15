package com.beckytidus.girlfriendmod.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BedBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.state.property.Properties;
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
        MINERAL_BLOCKS("mineral blocks", true, true),
        STORAGE("storage", true, false),
        PROCESSING("processing", false, false),
        REDSTONE("redstone", false, false),
        HOUSING("housing and decoration", true, false),
        SPECIAL("special blocks", false, true),
        DANGEROUS("dangerous blocks", false, true),
        WATER("water", true, false),
        LAVA("lava", true, true),
        PORTAL("portal", false, true),
        VILLAGE_STRUCTURES("village structures", false, false),
        NATURAL_FEATURES("natural features", true, false);

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

        // === MINERAL BLOCKS (Storage/Compressed forms) ===
        // Precious mineral blocks
        addBlock(Blocks.DIAMOND_BLOCK, BlockCategory.MINERAL_BLOCKS, "Diamond Block");
        addBlock(Blocks.EMERALD_BLOCK, BlockCategory.MINERAL_BLOCKS, "Emerald Block");
        addBlock(Blocks.NETHERITE_BLOCK, BlockCategory.MINERAL_BLOCKS, "Netherite Block");
        addBlock(Blocks.GOLD_BLOCK, BlockCategory.MINERAL_BLOCKS, "Gold Block");
        // Common mineral blocks
        addBlock(Blocks.IRON_BLOCK, BlockCategory.MINERAL_BLOCKS, "Iron Block");
        addBlock(Blocks.COPPER_BLOCK, BlockCategory.MINERAL_BLOCKS, "Copper Block");
        addBlock(Blocks.LAPIS_BLOCK, BlockCategory.MINERAL_BLOCKS, "Lapis Block");
        addBlock(Blocks.REDSTONE_BLOCK, BlockCategory.MINERAL_BLOCKS, "Redstone Block");
        addBlock(Blocks.COAL_BLOCK, BlockCategory.MINERAL_BLOCKS, "Coal Block");
        addBlock(Blocks.QUARTZ_BLOCK, BlockCategory.MINERAL_BLOCKS, "Quartz Block");
        // Raw mineral blocks
        addBlock(Blocks.RAW_IRON_BLOCK, BlockCategory.MINERAL_BLOCKS, "Raw Iron Block");
        addBlock(Blocks.RAW_GOLD_BLOCK, BlockCategory.MINERAL_BLOCKS, "Raw Gold Block");
        addBlock(Blocks.RAW_COPPER_BLOCK, BlockCategory.MINERAL_BLOCKS, "Raw Copper Block");
        // Amethyst
        addBlock(Blocks.AMETHYST_BLOCK, BlockCategory.MINERAL_BLOCKS, "Amethyst Block");
        addBlock(Blocks.BUDDING_AMETHYST, BlockCategory.MINERAL_BLOCKS, "Budding Amethyst");
        addBlock(Blocks.SMALL_AMETHYST_BUD, BlockCategory.MINERAL_BLOCKS, "Small Amethyst Bud");
        addBlock(Blocks.MEDIUM_AMETHYST_BUD, BlockCategory.MINERAL_BLOCKS, "Medium Amethyst Bud");
        addBlock(Blocks.LARGE_AMETHYST_BUD, BlockCategory.MINERAL_BLOCKS, "Large Amethyst Bud");
        addBlock(Blocks.AMETHYST_CLUSTER, BlockCategory.MINERAL_BLOCKS, "Amethyst Cluster");
        // Nether stars and other special
        addBlock(Blocks.SEA_LANTERN, BlockCategory.MINERAL_BLOCKS, "Sea Lantern");

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

        // === HOUSING - Glass ===
        addBlock(Blocks.GLASS, BlockCategory.HOUSING, "Glass");
        addBlock(Blocks.TINTED_GLASS, BlockCategory.HOUSING, "Tinted Glass");
        addBlock(Blocks.WHITE_STAINED_GLASS, BlockCategory.HOUSING, "White Stained Glass");
        addBlock(Blocks.ORANGE_STAINED_GLASS, BlockCategory.HOUSING, "Orange Stained Glass");
        addBlock(Blocks.MAGENTA_STAINED_GLASS, BlockCategory.HOUSING, "Magenta Stained Glass");
        addBlock(Blocks.LIGHT_BLUE_STAINED_GLASS, BlockCategory.HOUSING, "Light Blue Stained Glass");
        addBlock(Blocks.YELLOW_STAINED_GLASS, BlockCategory.HOUSING, "Yellow Stained Glass");
        addBlock(Blocks.LIME_STAINED_GLASS, BlockCategory.HOUSING, "Lime Stained Glass");
        addBlock(Blocks.PINK_STAINED_GLASS, BlockCategory.HOUSING, "Pink Stained Glass");
        addBlock(Blocks.GRAY_STAINED_GLASS, BlockCategory.HOUSING, "Gray Stained Glass");
        addBlock(Blocks.LIGHT_GRAY_STAINED_GLASS, BlockCategory.HOUSING, "Light Gray Stained Glass");
        addBlock(Blocks.CYAN_STAINED_GLASS, BlockCategory.HOUSING, "Cyan Stained Glass");
        addBlock(Blocks.PURPLE_STAINED_GLASS, BlockCategory.HOUSING, "Purple Stained Glass");
        addBlock(Blocks.BLUE_STAINED_GLASS, BlockCategory.HOUSING, "Blue Stained Glass");
        addBlock(Blocks.BROWN_STAINED_GLASS, BlockCategory.HOUSING, "Brown Stained Glass");
        addBlock(Blocks.GREEN_STAINED_GLASS, BlockCategory.HOUSING, "Green Stained Glass");
        addBlock(Blocks.RED_STAINED_GLASS, BlockCategory.HOUSING, "Red Stained Glass");
        addBlock(Blocks.BLACK_STAINED_GLASS, BlockCategory.HOUSING, "Black Stained Glass");
        // Glass Panes
        addBlock(Blocks.GLASS_PANE, BlockCategory.HOUSING, "Glass Pane");
        addBlock(Blocks.WHITE_STAINED_GLASS_PANE, BlockCategory.HOUSING, "White Stained Glass Pane");
        addBlock(Blocks.ORANGE_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Orange Stained Glass Pane");
        addBlock(Blocks.MAGENTA_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Magenta Stained Glass Pane");
        addBlock(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Light Blue Stained Glass Pane");
        addBlock(Blocks.YELLOW_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Yellow Stained Glass Pane");
        addBlock(Blocks.LIME_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Lime Stained Glass Pane");
        addBlock(Blocks.PINK_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Pink Stained Glass Pane");
        addBlock(Blocks.GRAY_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Gray Stained Glass Pane");
        addBlock(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Light Gray Stained Glass Pane");
        addBlock(Blocks.CYAN_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Cyan Stained Glass Pane");
        addBlock(Blocks.PURPLE_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Purple Stained Glass Pane");
        addBlock(Blocks.BLUE_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Blue Stained Glass Pane");
        addBlock(Blocks.BROWN_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Brown Stained Glass Pane");
        addBlock(Blocks.GREEN_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Green Stained Glass Pane");
        addBlock(Blocks.RED_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Red Stained Glass Pane");
        addBlock(Blocks.BLACK_STAINED_GLASS_PANE, BlockCategory.HOUSING, "Black Stained Glass Pane");

        // === HOUSING - Wool ===
        addBlock(Blocks.WHITE_WOOL, BlockCategory.HOUSING, "White Wool");
        addBlock(Blocks.ORANGE_WOOL, BlockCategory.HOUSING, "Orange Wool");
        addBlock(Blocks.MAGENTA_WOOL, BlockCategory.HOUSING, "Magenta Wool");
        addBlock(Blocks.LIGHT_BLUE_WOOL, BlockCategory.HOUSING, "Light Blue Wool");
        addBlock(Blocks.YELLOW_WOOL, BlockCategory.HOUSING, "Yellow Wool");
        addBlock(Blocks.LIME_WOOL, BlockCategory.HOUSING, "Lime Wool");
        addBlock(Blocks.PINK_WOOL, BlockCategory.HOUSING, "Pink Wool");
        addBlock(Blocks.GRAY_WOOL, BlockCategory.HOUSING, "Gray Wool");
        addBlock(Blocks.LIGHT_GRAY_WOOL, BlockCategory.HOUSING, "Light Gray Wool");
        addBlock(Blocks.CYAN_WOOL, BlockCategory.HOUSING, "Cyan Wool");
        addBlock(Blocks.PURPLE_WOOL, BlockCategory.HOUSING, "Purple Wool");
        addBlock(Blocks.BLUE_WOOL, BlockCategory.HOUSING, "Blue Wool");
        addBlock(Blocks.BROWN_WOOL, BlockCategory.HOUSING, "Brown Wool");
        addBlock(Blocks.GREEN_WOOL, BlockCategory.HOUSING, "Green Wool");
        addBlock(Blocks.RED_WOOL, BlockCategory.HOUSING, "Red Wool");
        addBlock(Blocks.BLACK_WOOL, BlockCategory.HOUSING, "Black Wool");

        // === HOUSING - Beds ===
        addBlock(Blocks.WHITE_BED, BlockCategory.HOUSING, "White Bed");
        addBlock(Blocks.ORANGE_BED, BlockCategory.HOUSING, "Orange Bed");
        addBlock(Blocks.MAGENTA_BED, BlockCategory.HOUSING, "Magenta Bed");
        addBlock(Blocks.LIGHT_BLUE_BED, BlockCategory.HOUSING, "Light Blue Bed");
        addBlock(Blocks.YELLOW_BED, BlockCategory.HOUSING, "Yellow Bed");
        addBlock(Blocks.LIME_BED, BlockCategory.HOUSING, "Lime Bed");
        addBlock(Blocks.PINK_BED, BlockCategory.HOUSING, "Pink Bed");
        addBlock(Blocks.GRAY_BED, BlockCategory.HOUSING, "Gray Bed");
        addBlock(Blocks.LIGHT_GRAY_BED, BlockCategory.HOUSING, "Light Gray Bed");
        addBlock(Blocks.CYAN_BED, BlockCategory.HOUSING, "Cyan Bed");
        addBlock(Blocks.PURPLE_BED, BlockCategory.HOUSING, "Purple Bed");
        addBlock(Blocks.BLUE_BED, BlockCategory.HOUSING, "Blue Bed");
        addBlock(Blocks.BROWN_BED, BlockCategory.HOUSING, "Brown Bed");
        addBlock(Blocks.GREEN_BED, BlockCategory.HOUSING, "Green Bed");
        addBlock(Blocks.RED_BED, BlockCategory.HOUSING, "Red Bed");
        addBlock(Blocks.BLACK_BED, BlockCategory.HOUSING, "Black Bed");

        // === HOUSING - Banners ===
        addBlock(Blocks.WHITE_BANNER, BlockCategory.HOUSING, "White Banner");
        addBlock(Blocks.ORANGE_BANNER, BlockCategory.HOUSING, "Orange Banner");
        addBlock(Blocks.MAGENTA_BANNER, BlockCategory.HOUSING, "Magenta Banner");
        addBlock(Blocks.LIGHT_BLUE_BANNER, BlockCategory.HOUSING, "Light Blue Banner");
        addBlock(Blocks.YELLOW_BANNER, BlockCategory.HOUSING, "Yellow Banner");
        addBlock(Blocks.LIME_BANNER, BlockCategory.HOUSING, "Lime Banner");
        addBlock(Blocks.PINK_BANNER, BlockCategory.HOUSING, "Pink Banner");
        addBlock(Blocks.GRAY_BANNER, BlockCategory.HOUSING, "Gray Banner");
        addBlock(Blocks.LIGHT_GRAY_BANNER, BlockCategory.HOUSING, "Light Gray Banner");
        addBlock(Blocks.CYAN_BANNER, BlockCategory.HOUSING, "Cyan Banner");
        addBlock(Blocks.PURPLE_BANNER, BlockCategory.HOUSING, "Purple Banner");
        addBlock(Blocks.BLUE_BANNER, BlockCategory.HOUSING, "Blue Banner");
        addBlock(Blocks.BROWN_BANNER, BlockCategory.HOUSING, "Brown Banner");
        addBlock(Blocks.GREEN_BANNER, BlockCategory.HOUSING, "Green Banner");
        addBlock(Blocks.RED_BANNER, BlockCategory.HOUSING, "Red Banner");
        addBlock(Blocks.BLACK_BANNER, BlockCategory.HOUSING, "Black Banner");
        // Wall Banners
        addBlock(Blocks.WHITE_WALL_BANNER, BlockCategory.HOUSING, "White Banner");
        addBlock(Blocks.ORANGE_WALL_BANNER, BlockCategory.HOUSING, "Orange Banner");
        addBlock(Blocks.MAGENTA_WALL_BANNER, BlockCategory.HOUSING, "Magenta Banner");
        addBlock(Blocks.LIGHT_BLUE_WALL_BANNER, BlockCategory.HOUSING, "Light Blue Banner");
        addBlock(Blocks.YELLOW_WALL_BANNER, BlockCategory.HOUSING, "Yellow Banner");
        addBlock(Blocks.LIME_WALL_BANNER, BlockCategory.HOUSING, "Lime Banner");
        addBlock(Blocks.PINK_WALL_BANNER, BlockCategory.HOUSING, "Pink Banner");
        addBlock(Blocks.GRAY_WALL_BANNER, BlockCategory.HOUSING, "Gray Banner");
        addBlock(Blocks.LIGHT_GRAY_WALL_BANNER, BlockCategory.HOUSING, "Light Gray Banner");
        addBlock(Blocks.CYAN_WALL_BANNER, BlockCategory.HOUSING, "Cyan Banner");
        addBlock(Blocks.PURPLE_WALL_BANNER, BlockCategory.HOUSING, "Purple Banner");
        addBlock(Blocks.BLUE_WALL_BANNER, BlockCategory.HOUSING, "Blue Banner");
        addBlock(Blocks.BROWN_WALL_BANNER, BlockCategory.HOUSING, "Brown Banner");
        addBlock(Blocks.GREEN_WALL_BANNER, BlockCategory.HOUSING, "Green Banner");
        addBlock(Blocks.RED_WALL_BANNER, BlockCategory.HOUSING, "Red Banner");
        addBlock(Blocks.BLACK_WALL_BANNER, BlockCategory.HOUSING, "Black Banner");

        // === HOUSING - Flower Pots ===
        addBlock(Blocks.FLOWER_POT, BlockCategory.HOUSING, "Flower Pot");
        addBlock(Blocks.POTTED_DANDELION, BlockCategory.HOUSING, "Potted Dandelion");
        addBlock(Blocks.POTTED_POPPY, BlockCategory.HOUSING, "Potted Poppy");
        addBlock(Blocks.POTTED_BLUE_ORCHID, BlockCategory.HOUSING, "Potted Blue Orchid");
        addBlock(Blocks.POTTED_ALLIUM, BlockCategory.HOUSING, "Potted Allium");
        addBlock(Blocks.POTTED_AZURE_BLUET, BlockCategory.HOUSING, "Potted Azure Bluet");
        addBlock(Blocks.POTTED_RED_TULIP, BlockCategory.HOUSING, "Potted Red Tulip");
        addBlock(Blocks.POTTED_ORANGE_TULIP, BlockCategory.HOUSING, "Potted Orange Tulip");
        addBlock(Blocks.POTTED_WHITE_TULIP, BlockCategory.HOUSING, "Potted White Tulip");
        addBlock(Blocks.POTTED_PINK_TULIP, BlockCategory.HOUSING, "Potted Pink Tulip");
        addBlock(Blocks.POTTED_OXEYE_DAISY, BlockCategory.HOUSING, "Potted Oxeye Daisy");
        addBlock(Blocks.POTTED_CORNFLOWER, BlockCategory.HOUSING, "Potted Cornflower");
        addBlock(Blocks.POTTED_LILY_OF_THE_VALLEY, BlockCategory.HOUSING, "Potted Lily of the Valley");
        addBlock(Blocks.POTTED_WITHER_ROSE, BlockCategory.HOUSING, "Potted Wither Rose");
        addBlock(Blocks.POTTED_TORCHFLOWER, BlockCategory.HOUSING, "Potted Torchflower");
        addBlock(Blocks.POTTED_OAK_SAPLING, BlockCategory.HOUSING, "Potted Oak Sapling");
        addBlock(Blocks.POTTED_SPRUCE_SAPLING, BlockCategory.HOUSING, "Potted Spruce Sapling");
        addBlock(Blocks.POTTED_BIRCH_SAPLING, BlockCategory.HOUSING, "Potted Birch Sapling");
        addBlock(Blocks.POTTED_JUNGLE_SAPLING, BlockCategory.HOUSING, "Potted Jungle Sapling");
        addBlock(Blocks.POTTED_ACACIA_SAPLING, BlockCategory.HOUSING, "Potted Acacia Sapling");
        addBlock(Blocks.POTTED_DARK_OAK_SAPLING, BlockCategory.HOUSING, "Potted Dark Oak Sapling");
        addBlock(Blocks.POTTED_CHERRY_SAPLING, BlockCategory.HOUSING, "Potted Cherry Sapling");
        addBlock(Blocks.POTTED_MANGROVE_PROPAGULE, BlockCategory.HOUSING, "Potted Mangrove Propagule");
        addBlock(Blocks.POTTED_FERN, BlockCategory.HOUSING, "Potted Fern");
        addBlock(Blocks.POTTED_DEAD_BUSH, BlockCategory.HOUSING, "Potted Dead Bush");
        addBlock(Blocks.POTTED_CACTUS, BlockCategory.HOUSING, "Potted Cactus");
        addBlock(Blocks.POTTED_BAMBOO, BlockCategory.HOUSING, "Potted Bamboo");
        addBlock(Blocks.POTTED_CRIMSON_FUNGUS, BlockCategory.HOUSING, "Potted Crimson Fungus");
        addBlock(Blocks.POTTED_WARPED_FUNGUS, BlockCategory.HOUSING, "Potted Warped Fungus");
        addBlock(Blocks.POTTED_CRIMSON_ROOTS, BlockCategory.HOUSING, "Potted Crimson Roots");
        addBlock(Blocks.POTTED_WARPED_ROOTS, BlockCategory.HOUSING, "Potted Warped Roots");
        addBlock(Blocks.POTTED_MANGROVE_PROPAGULE, BlockCategory.HOUSING, "Potted Mangrove Propagule");

        // === HOUSING - Lighting ===
        addBlock(Blocks.TORCH, BlockCategory.HOUSING, "Torch");
        addBlock(Blocks.WALL_TORCH, BlockCategory.HOUSING, "Wall Torch");
        addBlock(Blocks.SOUL_TORCH, BlockCategory.HOUSING, "Soul Torch");
        addBlock(Blocks.SOUL_WALL_TORCH, BlockCategory.HOUSING, "Soul Torch");
        addBlock(Blocks.LANTERN, BlockCategory.HOUSING, "Lantern");
        addBlock(Blocks.SOUL_LANTERN, BlockCategory.HOUSING, "Soul Lantern");
        addBlock(Blocks.GLOWSTONE, BlockCategory.HOUSING, "Glowstone");
        addBlock(Blocks.SHROOMLIGHT, BlockCategory.HOUSING, "Shroomlight");
        addBlock(Blocks.END_ROD, BlockCategory.HOUSING, "End Rod");
        addBlock(Blocks.LIGHT, BlockCategory.HOUSING, "Light Block");
        // Candles
        addBlock(Blocks.CANDLE, BlockCategory.HOUSING, "Candle");
        addBlock(Blocks.WHITE_CANDLE, BlockCategory.HOUSING, "White Candle");
        addBlock(Blocks.ORANGE_CANDLE, BlockCategory.HOUSING, "Orange Candle");
        addBlock(Blocks.MAGENTA_CANDLE, BlockCategory.HOUSING, "Magenta Candle");
        addBlock(Blocks.LIGHT_BLUE_CANDLE, BlockCategory.HOUSING, "Light Blue Candle");
        addBlock(Blocks.YELLOW_CANDLE, BlockCategory.HOUSING, "Yellow Candle");
        addBlock(Blocks.LIME_CANDLE, BlockCategory.HOUSING, "Lime Candle");
        addBlock(Blocks.PINK_CANDLE, BlockCategory.HOUSING, "Pink Candle");
        addBlock(Blocks.GRAY_CANDLE, BlockCategory.HOUSING, "Gray Candle");
        addBlock(Blocks.LIGHT_GRAY_CANDLE, BlockCategory.HOUSING, "Light Gray Candle");
        addBlock(Blocks.CYAN_CANDLE, BlockCategory.HOUSING, "Cyan Candle");
        addBlock(Blocks.PURPLE_CANDLE, BlockCategory.HOUSING, "Purple Candle");
        addBlock(Blocks.BLUE_CANDLE, BlockCategory.HOUSING, "Blue Candle");
        addBlock(Blocks.BROWN_CANDLE, BlockCategory.HOUSING, "Brown Candle");
        addBlock(Blocks.GREEN_CANDLE, BlockCategory.HOUSING, "Green Candle");
        addBlock(Blocks.RED_CANDLE, BlockCategory.HOUSING, "Red Candle");
        addBlock(Blocks.BLACK_CANDLE, BlockCategory.HOUSING, "Black Candle");
        // Candle Cakes
        addBlock(Blocks.CANDLE_CAKE, BlockCategory.HOUSING, "Candle Cake");
        addBlock(Blocks.WHITE_CANDLE_CAKE, BlockCategory.HOUSING, "White Candle Cake");
        addBlock(Blocks.ORANGE_CANDLE_CAKE, BlockCategory.HOUSING, "Orange Candle Cake");
        addBlock(Blocks.MAGENTA_CANDLE_CAKE, BlockCategory.HOUSING, "Magenta Candle Cake");
        addBlock(Blocks.LIGHT_BLUE_CANDLE_CAKE, BlockCategory.HOUSING, "Light Blue Candle Cake");
        addBlock(Blocks.YELLOW_CANDLE_CAKE, BlockCategory.HOUSING, "Yellow Candle Cake");
        addBlock(Blocks.LIME_CANDLE_CAKE, BlockCategory.HOUSING, "Lime Candle Cake");
        addBlock(Blocks.PINK_CANDLE_CAKE, BlockCategory.HOUSING, "Pink Candle Cake");
        addBlock(Blocks.GRAY_CANDLE_CAKE, BlockCategory.HOUSING, "Gray Candle Cake");
        addBlock(Blocks.LIGHT_GRAY_CANDLE_CAKE, BlockCategory.HOUSING, "Light Gray Candle Cake");
        addBlock(Blocks.CYAN_CANDLE_CAKE, BlockCategory.HOUSING, "Cyan Candle Cake");
        addBlock(Blocks.PURPLE_CANDLE_CAKE, BlockCategory.HOUSING, "Purple Candle Cake");
        addBlock(Blocks.BLUE_CANDLE_CAKE, BlockCategory.HOUSING, "Blue Candle Cake");
        addBlock(Blocks.BROWN_CANDLE_CAKE, BlockCategory.HOUSING, "Brown Candle Cake");
        addBlock(Blocks.GREEN_CANDLE_CAKE, BlockCategory.HOUSING, "Green Candle Cake");
        addBlock(Blocks.RED_CANDLE_CAKE, BlockCategory.HOUSING, "Red Candle Cake");
        addBlock(Blocks.BLACK_CANDLE_CAKE, BlockCategory.HOUSING, "Black Candle Cake");

        // === HOUSING - Doors ===
        addBlock(Blocks.OAK_DOOR, BlockCategory.HOUSING, "Oak Door");
        addBlock(Blocks.SPRUCE_DOOR, BlockCategory.HOUSING, "Spruce Door");
        addBlock(Blocks.BIRCH_DOOR, BlockCategory.HOUSING, "Birch Door");
        addBlock(Blocks.JUNGLE_DOOR, BlockCategory.HOUSING, "Jungle Door");
        addBlock(Blocks.ACACIA_DOOR, BlockCategory.HOUSING, "Acacia Door");
        addBlock(Blocks.DARK_OAK_DOOR, BlockCategory.HOUSING, "Dark Oak Door");
        addBlock(Blocks.CHERRY_DOOR, BlockCategory.HOUSING, "Cherry Door");
        addBlock(Blocks.MANGROVE_DOOR, BlockCategory.HOUSING, "Mangrove Door");
        addBlock(Blocks.BAMBOO_DOOR, BlockCategory.HOUSING, "Bamboo Door");
        addBlock(Blocks.CRIMSON_DOOR, BlockCategory.HOUSING, "Crimson Door");
        addBlock(Blocks.WARPED_DOOR, BlockCategory.HOUSING, "Warped Door");
        addBlock(Blocks.IRON_DOOR, BlockCategory.HOUSING, "Iron Door");
        addBlock(Blocks.COPPER_DOOR, BlockCategory.HOUSING, "Copper Door");

        // === HOUSING - Trapdoors ===
        addBlock(Blocks.OAK_TRAPDOOR, BlockCategory.HOUSING, "Oak Trapdoor");
        addBlock(Blocks.SPRUCE_TRAPDOOR, BlockCategory.HOUSING, "Spruce Trapdoor");
        addBlock(Blocks.BIRCH_TRAPDOOR, BlockCategory.HOUSING, "Birch Trapdoor");
        addBlock(Blocks.JUNGLE_TRAPDOOR, BlockCategory.HOUSING, "Jungle Trapdoor");
        addBlock(Blocks.ACACIA_TRAPDOOR, BlockCategory.HOUSING, "Acacia Trapdoor");
        addBlock(Blocks.DARK_OAK_TRAPDOOR, BlockCategory.HOUSING, "Dark Oak Trapdoor");
        addBlock(Blocks.CHERRY_TRAPDOOR, BlockCategory.HOUSING, "Cherry Trapdoor");
        addBlock(Blocks.MANGROVE_TRAPDOOR, BlockCategory.HOUSING, "Mangrove Trapdoor");
        addBlock(Blocks.BAMBOO_TRAPDOOR, BlockCategory.HOUSING, "Bamboo Trapdoor");
        addBlock(Blocks.CRIMSON_TRAPDOOR, BlockCategory.HOUSING, "Crimson Trapdoor");
        addBlock(Blocks.WARPED_TRAPDOOR, BlockCategory.HOUSING, "Warped Trapdoor");
        addBlock(Blocks.IRON_TRAPDOOR, BlockCategory.HOUSING, "Iron Trapdoor");
        addBlock(Blocks.COPPER_TRAPDOOR, BlockCategory.HOUSING, "Copper Trapdoor");

        // === HOUSING - Fence Gates ===
        addBlock(Blocks.OAK_FENCE_GATE, BlockCategory.HOUSING, "Oak Fence Gate");
        addBlock(Blocks.SPRUCE_FENCE_GATE, BlockCategory.HOUSING, "Spruce Fence Gate");
        addBlock(Blocks.BIRCH_FENCE_GATE, BlockCategory.HOUSING, "Birch Fence Gate");
        addBlock(Blocks.JUNGLE_FENCE_GATE, BlockCategory.HOUSING, "Jungle Fence Gate");
        addBlock(Blocks.ACACIA_FENCE_GATE, BlockCategory.HOUSING, "Acacia Fence Gate");
        addBlock(Blocks.DARK_OAK_FENCE_GATE, BlockCategory.HOUSING, "Dark Oak Fence Gate");
        addBlock(Blocks.CHERRY_FENCE_GATE, BlockCategory.HOUSING, "Cherry Fence Gate");
        addBlock(Blocks.MANGROVE_FENCE_GATE, BlockCategory.HOUSING, "Mangrove Fence Gate");
        addBlock(Blocks.BAMBOO_FENCE_GATE, BlockCategory.HOUSING, "Bamboo Fence Gate");
        addBlock(Blocks.CRIMSON_FENCE_GATE, BlockCategory.HOUSING, "Crimson Fence Gate");
        addBlock(Blocks.WARPED_FENCE_GATE, BlockCategory.HOUSING, "Warped Fence Gate");

        // === HOUSING - Carpets ===
        addBlock(Blocks.WHITE_CARPET, BlockCategory.HOUSING, "White Carpet");
        addBlock(Blocks.ORANGE_CARPET, BlockCategory.HOUSING, "Orange Carpet");
        addBlock(Blocks.MAGENTA_CARPET, BlockCategory.HOUSING, "Magenta Carpet");
        addBlock(Blocks.LIGHT_BLUE_CARPET, BlockCategory.HOUSING, "Light Blue Carpet");
        addBlock(Blocks.YELLOW_CARPET, BlockCategory.HOUSING, "Yellow Carpet");
        addBlock(Blocks.LIME_CARPET, BlockCategory.HOUSING, "Lime Carpet");
        addBlock(Blocks.PINK_CARPET, BlockCategory.HOUSING, "Pink Carpet");
        addBlock(Blocks.GRAY_CARPET, BlockCategory.HOUSING, "Gray Carpet");
        addBlock(Blocks.LIGHT_GRAY_CARPET, BlockCategory.HOUSING, "Light Gray Carpet");
        addBlock(Blocks.CYAN_CARPET, BlockCategory.HOUSING, "Cyan Carpet");
        addBlock(Blocks.PURPLE_CARPET, BlockCategory.HOUSING, "Purple Carpet");
        addBlock(Blocks.BLUE_CARPET, BlockCategory.HOUSING, "Blue Carpet");
        addBlock(Blocks.BROWN_CARPET, BlockCategory.HOUSING, "Brown Carpet");
        addBlock(Blocks.GREEN_CARPET, BlockCategory.HOUSING, "Green Carpet");
        addBlock(Blocks.RED_CARPET, BlockCategory.HOUSING, "Red Carpet");
        addBlock(Blocks.BLACK_CARPET, BlockCategory.HOUSING, "Black Carpet");

        // === HOUSING - Bookshelves and Decorative ===
        addBlock(Blocks.BOOKSHELF, BlockCategory.HOUSING, "Bookshelf");
        addBlock(Blocks.CHISELED_BOOKSHELF, BlockCategory.HOUSING, "Chiseled Bookshelf");
        // Note: Paintings, Item Frames, and Armor Stands are entities, not blocks

        // === NATURAL_FEATURES - Farm Crops ===
        addBlock(Blocks.WHEAT, BlockCategory.NATURAL_FEATURES, "Wheat");
        addBlock(Blocks.CARROTS, BlockCategory.NATURAL_FEATURES, "Carrots");
        addBlock(Blocks.POTATOES, BlockCategory.NATURAL_FEATURES, "Potatoes");
        addBlock(Blocks.BEETROOTS, BlockCategory.NATURAL_FEATURES, "Beetroots");
        addBlock(Blocks.PUMPKIN, BlockCategory.NATURAL_FEATURES, "Pumpkin");
        addBlock(Blocks.CARVED_PUMPKIN, BlockCategory.NATURAL_FEATURES, "Carved Pumpkin");
        addBlock(Blocks.JACK_O_LANTERN, BlockCategory.NATURAL_FEATURES, "Jack o'Lantern");
        addBlock(Blocks.PUMPKIN_STEM, BlockCategory.NATURAL_FEATURES, "Pumpkin Stem");
        addBlock(Blocks.ATTACHED_PUMPKIN_STEM, BlockCategory.NATURAL_FEATURES, "Pumpkin Stem");
        addBlock(Blocks.MELON, BlockCategory.NATURAL_FEATURES, "Melon");
        addBlock(Blocks.MELON_STEM, BlockCategory.NATURAL_FEATURES, "Melon Stem");
        addBlock(Blocks.ATTACHED_MELON_STEM, BlockCategory.NATURAL_FEATURES, "Melon Stem");
        addBlock(Blocks.SUGAR_CANE, BlockCategory.NATURAL_FEATURES, "Sugar Cane");
        addBlock(Blocks.BAMBOO, BlockCategory.NATURAL_FEATURES, "Bamboo");
        addBlock(Blocks.BAMBOO_SAPLING, BlockCategory.NATURAL_FEATURES, "Bamboo Sapling");
        addBlock(Blocks.FARMLAND, BlockCategory.NATURAL_FEATURES, "Farmland");
        // Sweet Berry Bush and Cactus are already in DANGEROUS, but also add to NATURAL_FEATURES for context
        // Note: A block can only have one category, so we keep them in DANGEROUS since they can hurt you

        // === NATURAL_FEATURES - Mushrooms & Fungi ===
        addBlock(Blocks.BROWN_MUSHROOM, BlockCategory.NATURAL_FEATURES, "Brown Mushroom");
        addBlock(Blocks.RED_MUSHROOM, BlockCategory.NATURAL_FEATURES, "Red Mushroom");
        addBlock(Blocks.BROWN_MUSHROOM_BLOCK, BlockCategory.NATURAL_FEATURES, "Brown Mushroom Block");
        addBlock(Blocks.RED_MUSHROOM_BLOCK, BlockCategory.NATURAL_FEATURES, "Red Mushroom Block");
        addBlock(Blocks.MUSHROOM_STEM, BlockCategory.NATURAL_FEATURES, "Mushroom Stem");
        addBlock(Blocks.CRIMSON_FUNGUS, BlockCategory.NATURAL_FEATURES, "Crimson Fungus");
        addBlock(Blocks.WARPED_FUNGUS, BlockCategory.NATURAL_FEATURES, "Warped Fungus");
        addBlock(Blocks.CRIMSON_ROOTS, BlockCategory.NATURAL_FEATURES, "Crimson Roots");
        addBlock(Blocks.WARPED_ROOTS, BlockCategory.NATURAL_FEATURES, "Warped Roots");
        addBlock(Blocks.NETHER_WART, BlockCategory.NATURAL_FEATURES, "Nether Wart");

        // === NATURAL_FEATURES - Tree Saplings ===
        addBlock(Blocks.OAK_SAPLING, BlockCategory.NATURAL_FEATURES, "Oak Sapling");
        addBlock(Blocks.SPRUCE_SAPLING, BlockCategory.NATURAL_FEATURES, "Spruce Sapling");
        addBlock(Blocks.BIRCH_SAPLING, BlockCategory.NATURAL_FEATURES, "Birch Sapling");
        addBlock(Blocks.JUNGLE_SAPLING, BlockCategory.NATURAL_FEATURES, "Jungle Sapling");
        addBlock(Blocks.ACACIA_SAPLING, BlockCategory.NATURAL_FEATURES, "Acacia Sapling");
        addBlock(Blocks.DARK_OAK_SAPLING, BlockCategory.NATURAL_FEATURES, "Dark Oak Sapling");
        addBlock(Blocks.CHERRY_SAPLING, BlockCategory.NATURAL_FEATURES, "Cherry Sapling");
        addBlock(Blocks.MANGROVE_PROPAGULE, BlockCategory.NATURAL_FEATURES, "Mangrove Propagule");
        addBlock(Blocks.PALE_OAK_SAPLING, BlockCategory.NATURAL_FEATURES, "Pale Oak Sapling");

        // === NATURAL_FEATURES - Tree Leaves ===
        addBlock(Blocks.OAK_LEAVES, BlockCategory.NATURAL_FEATURES, "Oak Leaves");
        addBlock(Blocks.SPRUCE_LEAVES, BlockCategory.NATURAL_FEATURES, "Spruce Leaves");
        addBlock(Blocks.BIRCH_LEAVES, BlockCategory.NATURAL_FEATURES, "Birch Leaves");
        addBlock(Blocks.JUNGLE_LEAVES, BlockCategory.NATURAL_FEATURES, "Jungle Leaves");
        addBlock(Blocks.ACACIA_LEAVES, BlockCategory.NATURAL_FEATURES, "Acacia Leaves");
        addBlock(Blocks.DARK_OAK_LEAVES, BlockCategory.NATURAL_FEATURES, "Dark Oak Leaves");
        addBlock(Blocks.CHERRY_LEAVES, BlockCategory.NATURAL_FEATURES, "Cherry Leaves");
        addBlock(Blocks.MANGROVE_LEAVES, BlockCategory.NATURAL_FEATURES, "Mangrove Leaves");
        addBlock(Blocks.PALE_OAK_LEAVES, BlockCategory.NATURAL_FEATURES, "Pale Oak Leaves");
        addBlock(Blocks.AZALEA_LEAVES, BlockCategory.NATURAL_FEATURES, "Azalea Leaves");
        // Note: FLOWERING_AZALEA and FLOWERING_AZALEA_LEAVES are already in FLOWERS

        // === NATURAL_FEATURES - Ocean Features ===
        addBlock(Blocks.SEA_PICKLE, BlockCategory.NATURAL_FEATURES, "Sea Pickle");
        addBlock(Blocks.SEAGRASS, BlockCategory.NATURAL_FEATURES, "Seagrass");
        addBlock(Blocks.TALL_SEAGRASS, BlockCategory.NATURAL_FEATURES, "Tall Seagrass");
        addBlock(Blocks.KELP, BlockCategory.NATURAL_FEATURES, "Kelp");
        addBlock(Blocks.KELP_PLANT, BlockCategory.NATURAL_FEATURES, "Kelp");
        // Coral
        addBlock(Blocks.TUBE_CORAL, BlockCategory.NATURAL_FEATURES, "Tube Coral");
        addBlock(Blocks.BRAIN_CORAL, BlockCategory.NATURAL_FEATURES, "Brain Coral");
        addBlock(Blocks.BUBBLE_CORAL, BlockCategory.NATURAL_FEATURES, "Bubble Coral");
        addBlock(Blocks.FIRE_CORAL, BlockCategory.NATURAL_FEATURES, "Fire Coral");
        addBlock(Blocks.HORN_CORAL, BlockCategory.NATURAL_FEATURES, "Horn Coral");
        // Coral Fans
        addBlock(Blocks.TUBE_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Tube Coral Fan");
        addBlock(Blocks.BRAIN_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Brain Coral Fan");
        addBlock(Blocks.BUBBLE_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Bubble Coral Fan");
        addBlock(Blocks.FIRE_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Fire Coral Fan");
        addBlock(Blocks.HORN_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Horn Coral Fan");
        // Dead Coral
        addBlock(Blocks.DEAD_TUBE_CORAL, BlockCategory.NATURAL_FEATURES, "Dead Tube Coral");
        addBlock(Blocks.DEAD_BRAIN_CORAL, BlockCategory.NATURAL_FEATURES, "Dead Brain Coral");
        addBlock(Blocks.DEAD_BUBBLE_CORAL, BlockCategory.NATURAL_FEATURES, "Dead Bubble Coral");
        addBlock(Blocks.DEAD_FIRE_CORAL, BlockCategory.NATURAL_FEATURES, "Dead Fire Coral");
        addBlock(Blocks.DEAD_HORN_CORAL, BlockCategory.NATURAL_FEATURES, "Dead Horn Coral");
        addBlock(Blocks.DEAD_TUBE_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Dead Tube Coral Fan");
        addBlock(Blocks.DEAD_BRAIN_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Dead Brain Coral Fan");
        addBlock(Blocks.DEAD_BUBBLE_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Dead Bubble Coral Fan");
        addBlock(Blocks.DEAD_FIRE_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Dead Fire Coral Fan");
        addBlock(Blocks.DEAD_HORN_CORAL_FAN, BlockCategory.NATURAL_FEATURES, "Dead Horn Coral Fan");

        // === NATURAL_FEATURES - Nether Features ===
        addBlock(Blocks.CRIMSON_NYLIUM, BlockCategory.NATURAL_FEATURES, "Crimson Nylium");
        addBlock(Blocks.WARPED_NYLIUM, BlockCategory.NATURAL_FEATURES, "Warped Nylium");
        addBlock(Blocks.NETHERRACK, BlockCategory.NATURAL_FEATURES, "Netherrack");
        addBlock(Blocks.BASALT, BlockCategory.NATURAL_FEATURES, "Basalt");
        addBlock(Blocks.POLISHED_BASALT, BlockCategory.NATURAL_FEATURES, "Polished Basalt");
        addBlock(Blocks.SMOOTH_BASALT, BlockCategory.NATURAL_FEATURES, "Smooth Basalt");
        addBlock(Blocks.BLACKSTONE, BlockCategory.NATURAL_FEATURES, "Blackstone");
        addBlock(Blocks.GLOWSTONE, BlockCategory.NATURAL_FEATURES, "Glowstone");
        // Note: SHROOMLIGHT is already in HOUSING/Lighting
        addBlock(Blocks.WEEPING_VINES, BlockCategory.NATURAL_FEATURES, "Weeping Vines");
        addBlock(Blocks.WEEPING_VINES_PLANT, BlockCategory.NATURAL_FEATURES, "Weeping Vines");
        addBlock(Blocks.TWISTING_VINES, BlockCategory.NATURAL_FEATURES, "Twisting Vines");
        addBlock(Blocks.TWISTING_VINES_PLANT, BlockCategory.NATURAL_FEATURES, "Twisting Vines");

        // === NATURAL_FEATURES - End Features ===
        addBlock(Blocks.CHORUS_PLANT, BlockCategory.NATURAL_FEATURES, "Chorus Plant");
        // Note: CHORUS_FLOWER is already in FLOWERS
        addBlock(Blocks.END_STONE, BlockCategory.NATURAL_FEATURES, "End Stone");

        // === NATURAL_FEATURES - Other Natural Plants ===
        addBlock(Blocks.VINE, BlockCategory.NATURAL_FEATURES, "Vines");
        addBlock(Blocks.LILY_PAD, BlockCategory.NATURAL_FEATURES, "Lily Pad");
        addBlock(Blocks.DEAD_BUSH, BlockCategory.NATURAL_FEATURES, "Dead Bush");
        addBlock(Blocks.FERN, BlockCategory.NATURAL_FEATURES, "Fern");
        addBlock(Blocks.LARGE_FERN, BlockCategory.NATURAL_FEATURES, "Large Fern");
        addBlock(Blocks.TALL_GRASS, BlockCategory.NATURAL_FEATURES, "Tall Grass");
        addBlock(Blocks.SHORT_GRASS, BlockCategory.NATURAL_FEATURES, "Grass");
        addBlock(Blocks.HANGING_ROOTS, BlockCategory.NATURAL_FEATURES, "Hanging Roots");
        addBlock(Blocks.SPORE_BLOSSOM, BlockCategory.NATURAL_FEATURES, "Spore Blossom");
        addBlock(Blocks.BIG_DRIPLEAF, BlockCategory.NATURAL_FEATURES, "Big Dripleaf");
        addBlock(Blocks.BIG_DRIPLEAF_STEM, BlockCategory.NATURAL_FEATURES, "Big Dripleaf Stem");
        addBlock(Blocks.SMALL_DRIPLEAF, BlockCategory.NATURAL_FEATURES, "Small Dripleaf");
        addBlock(Blocks.MOSS_BLOCK, BlockCategory.NATURAL_FEATURES, "Moss Block");
        addBlock(Blocks.MOSS_CARPET, BlockCategory.NATURAL_FEATURES, "Moss Carpet");
        addBlock(Blocks.PINK_PETALS, BlockCategory.NATURAL_FEATURES, "Pink Petals");
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
     * Check if this is the "main" part of a multi-block structure.
     * This prevents counting beds, doors, tall flowers, and banners twice.
     * 
     * @param state The block state to check
     * @return true if this is the main part that should be counted, false if it's a secondary part
     */
    private static boolean isMainPartOfMultiBlock(BlockState state) {
        Block block = state.getBlock();
        
        // Beds: Only count the HEAD part (not the FOOT)
        if (block instanceof BedBlock) {
            return state.getOrEmpty(Properties.BED_PART)
                .map(part -> part == BedPart.HEAD)
                .orElse(true);
        }
        
        // Doors: Only count the LOWER half (not the UPPER)
        if (block instanceof DoorBlock) {
            return state.getOrEmpty(Properties.DOUBLE_BLOCK_HALF)
                .map(half -> half == DoubleBlockHalf.LOWER)
                .orElse(true);
        }
        
        // Tall flowers (sunflower, lilac, peony, rose bush, pitcher plant): Only count LOWER half
        if (block instanceof TallPlantBlock) {
            return state.getOrEmpty(Properties.DOUBLE_BLOCK_HALF)
                .map(half -> half == DoubleBlockHalf.LOWER)
                .orElse(true);
        }
        
        // Standing banners are 2 blocks tall - only count the base
        // Wall banners are 1 block, so always count them
        // We can detect standing banners by checking if they have the ROTATION property
        // (wall banners are attached to a wall, standing banners have ROTATION)
        // For simplicity, we'll skip this check as banners are less critical
        
        return true; // For all other blocks, count them
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

                    // Skip secondary parts of multi-block structures (beds, doors, tall flowers)
                    if (!isMainPartOfMultiBlock(state)) continue;

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
            case MINERAL_BLOCKS -> {
                // React to valuable mineral blocks (diamond, emerald, netherite)
                if (mostInteresting.block == Blocks.DIAMOND_BLOCK || 
                    mostInteresting.block == Blocks.EMERALD_BLOCK || 
                    mostInteresting.block == Blocks.NETHERITE_BLOCK) {
                    yield girlfriendName + " is amazed by the " + mostInteresting.displayName + " " + location + "! That's incredibly valuable - react with excitement and awe!";
                }
                yield girlfriendName + " noticed a " + mostInteresting.displayName + " " + location + ". Comment appreciatively on the stored resources.";
            }
            case FLOWERS -> girlfriendName + " noticed some beautiful " + mostInteresting.displayName + "s " + location + ". React with appreciation for the pretty flowers.";
            case HOUSING -> girlfriendName + " noticed " + mostInteresting.displayName + " " + location + ". Comment positively on the cozy home decoration or building materials.";
            case STORAGE -> girlfriendName + " sees a " + mostInteresting.displayName + " " + location + ". Show mild curiosity about what might be inside.";
            case SPECIAL -> girlfriendName + " discovered a " + mostInteresting.displayName + " " + location + "! React with excitement or awe at this special find.";
            case LAVA, DANGEROUS -> girlfriendName + " noticed " + mostInteresting.displayName + " " + location + ". Warn the player to be careful!";
            case PORTAL -> girlfriendName + " sees a " + mostInteresting.displayName + " " + location + ". React with wonder or curiosity.";
            case WATER -> null; // Don't react to water
            case NATURAL_FEATURES -> generateNaturalFeatureReaction(mostInteresting, girlfriendName, location);
            default -> girlfriendName + " noticed " + mostInteresting.displayName + " " + location + ". Briefly acknowledge it.";
        };
    }

    /**
     * Generate a reaction prompt for natural feature blocks.
     */
    private static String generateNaturalFeatureReaction(DetectedBlock block, String girlfriendName, String location) {
        // Farm crops
        if (block.block == Blocks.WHEAT || block.block == Blocks.CARROTS || 
            block.block == Blocks.POTATOES || block.block == Blocks.BEETROOTS) {
            return girlfriendName + " notices a farm with " + block.displayName + " growing " + location + ". Comment positively on the farming efforts.";
        }
        if (block.block == Blocks.PUMPKIN || block.block == Blocks.MELON) {
            return girlfriendName + " spots a " + block.displayName + " " + location + ". React with mild interest at the grown produce.";
        }
        if (block.block == Blocks.FARMLAND) {
            return girlfriendName + " sees tilled farmland " + location + ". Acknowledge the prepared farming area.";
        }
        if (block.block == Blocks.SUGAR_CANE) {
            return girlfriendName + " notices Sugar Cane growing " + location + ". Briefly mention it could be useful for paper or sugar.";
        }
        if (block.block == Blocks.BAMBOO) {
            return girlfriendName + " sees tall Bamboo " + location + ". Comment on how useful bamboo can be.";
        }
        
        // Mushrooms
        if (block.block == Blocks.BROWN_MUSHROOM || block.block == Blocks.RED_MUSHROOM) {
            return girlfriendName + " spotted a " + block.displayName + " " + location + ". React with mild curiosity about the fungi.";
        }
        if (block.block == Blocks.CRIMSON_FUNGUS || block.block == Blocks.WARPED_FUNGUS) {
            return girlfriendName + " notices a strange " + block.displayName + " " + location + ". React with curiosity about this Nether fungus.";
        }
        if (block.block == Blocks.NETHER_WART) {
            return girlfriendName + " sees Nether Wart growing " + location + ". Acknowledge this valuable brewing ingredient.";
        }
        
        // Trees
        if (block.displayName.contains("Sapling") || block.displayName.contains("Propagule")) {
            return girlfriendName + " notices a " + block.displayName + " " + location + ". Comment on how it will grow into a tree someday.";
        }
        if (block.displayName.contains("Leaves")) {
            return girlfriendName + " sees " + block.displayName + " " + location + ". Appreciate the natural beauty of the tree.";
        }
        
        // Ocean features
        if (block.block == Blocks.SEA_PICKLE) {
            return girlfriendName + " spots Sea Pickles " + location + ". Comment on their soft glow underwater.";
        }
        if (block.block == Blocks.SEAGRASS || block.block == Blocks.TALL_SEAGRASS) {
            return girlfriendName + " notices Seagrass swaying " + location + ". Briefly appreciate the underwater flora.";
        }
        if (block.block == Blocks.KELP || block.block == Blocks.KELP_PLANT) {
            return girlfriendName + " sees Kelp growing " + location + ". Mention it could be dried for food.";
        }
        if (block.displayName.contains("Coral") && !block.displayName.contains("Dead")) {
            return girlfriendName + " spots colorful " + block.displayName + " " + location + ". React with appreciation for the beautiful ocean coral.";
        }
        if (block.displayName.contains("Dead Coral")) {
            return girlfriendName + " notices dead coral " + location + ". Express mild sadness that it's no longer alive.";
        }
        
        // Nether features
        if (block.block == Blocks.CRIMSON_NYLIUM || block.block == Blocks.WARPED_NYLIUM) {
            return girlfriendName + " walks on " + block.displayName + " " + location + ". Comment on the strange Nether ground.";
        }
        if (block.block == Blocks.GLOWSTONE) {
            return girlfriendName + " sees the warm glow of Glowstone " + location + ". Appreciate the light in the darkness.";
        }
        if (block.block == Blocks.WEEPING_VINES || block.block == Blocks.TWISTING_VINES) {
            return girlfriendName + " notices " + block.displayName + " hanging " + location + ". Comment on the strange Nether plants.";
        }
        if (block.block == Blocks.BASALT || block.block == Blocks.POLISHED_BASALT || block.block == Blocks.SMOOTH_BASALT) {
            return girlfriendName + " sees " + block.displayName + " " + location + ". Note the volcanic rock of the Nether.";
        }
        
        // End features
        if (block.block == Blocks.CHORUS_PLANT) {
            return girlfriendName + " notices a Chorus Plant " + location + ". React with curiosity about this End-dimension flora.";
        }
        if (block.block == Blocks.END_STONE) {
            return girlfriendName + " walks on the strange yellow End Stone " + location + ". Comment on the alien feeling of this dimension.";
        }
        
        // Other natural plants
        if (block.block == Blocks.LILY_PAD) {
            return girlfriendName + " sees Lily Pads floating " + location + ". Appreciate the peaceful water plants.";
        }
        if (block.block == Blocks.VINE) {
            return girlfriendName + " notices Vines hanging " + location + ". Comment on the natural greenery.";
        }
        if (block.block == Blocks.MOSS_BLOCK || block.block == Blocks.MOSS_CARPET) {
            return girlfriendName + " feels the soft " + block.displayName + " " + location + ". Appreciate the lush green moss.";
        }
        if (block.block == Blocks.BIG_DRIPLEAF || block.block == Blocks.SMALL_DRIPLEAF) {
            return girlfriendName + " sees " + block.displayName + " " + location + ". Comment on the interesting cave plant.";
        }
        if (block.block == Blocks.SPORE_BLOSSOM) {
            return girlfriendName + " admires the Spore Blossom " + location + ". React with wonder at the beautiful particles.";
        }
        
        // Default for other natural features
        return girlfriendName + " notices " + block.displayName + " " + location + ". Briefly acknowledge the natural surroundings.";
    }
}