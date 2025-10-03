package net.geraldhofbauer.vanillaplusadditions.modules;

import net.geraldhofbauer.vanillaplusadditions.VanillaPlusAdditions;
import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Improved Storage Module
 * 
 * Adds enhanced storage solutions that maintain vanilla aesthetics:
 * - Reinforced chest variants
 * - Better organization tools
 * - Storage-related quality-of-life improvements
 */
public class ImprovedStorageModule extends AbstractModule {
    
    // Deferred registers for this module
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VanillaPlusAdditions.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VanillaPlusAdditions.MODID);
    
    // Example blocks for this module
    public static final DeferredBlock<Block> REINFORCED_CHEST = BLOCKS.registerSimpleBlock("reinforced_chest",
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.5F)
            .sound(net.minecraft.world.level.block.SoundType.WOOD));
    
    // Block items
    public static final DeferredItem<BlockItem> REINFORCED_CHEST_ITEM = ITEMS.registerSimpleBlockItem("reinforced_chest", REINFORCED_CHEST);
    
    // Storage-related items
    public static final DeferredItem<Item> STORAGE_LABEL = ITEMS.registerSimpleItem("storage_label",
        new Item.Properties().stacksTo(64));
    
    public ImprovedStorageModule() {
        super("improved_storage", "Improved Storage", 
              "Enhances storage options with reinforced containers and organization tools");
    }
    
    @Override
    protected void onInitialize() {
        // Register our deferred registers
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        
        logger.info("Improved Storage module registered {} blocks and {} items", 
                   BLOCKS.getEntries().size(), ITEMS.getEntries().size());
    }
    
    @Override
    protected void onCommonSetup() {
        // Here we could add recipes, loot tables, etc.
        logger.debug("Improved Storage module common setup complete");
    }
    
    @Override
    protected void onClientSetup() {
        // Client-side rendering setup would go here
        logger.debug("Improved Storage module client setup complete");
    }
    
    @Override
    public boolean isEnabledByDefault() {
        // This module might be more experimental, so disabled by default
        return false;
    }
}