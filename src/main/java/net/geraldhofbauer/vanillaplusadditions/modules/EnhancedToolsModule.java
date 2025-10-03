package net.geraldhofbauer.vanillaplusadditions.modules;

import net.geraldhofbauer.vanillaplusadditions.VanillaPlusAdditions;
import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Enhanced Tools Module
 * 
 * Adds quality-of-life improvements to vanilla tools, such as:
 * - Better tool durability information
 * - Enhanced tool tooltips
 * - Potential new tool variants that fit vanilla style
 */
public class EnhancedToolsModule extends AbstractModule {
    
    // Deferred registers for this module
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VanillaPlusAdditions.MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VanillaPlusAdditions.MODID);
    
    // Example items for this module
    public static final DeferredItem<Item> REINFORCED_STICK = ITEMS.registerSimpleItem("reinforced_stick", 
        new Item.Properties().durability(128));
    
    // Creative tab for this module's items
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ENHANCED_TOOLS_TAB = CREATIVE_TABS.register("enhanced_tools", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.vanillaplusadditions.enhanced_tools"))
            .icon(() -> REINFORCED_STICK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(REINFORCED_STICK.get());
            }).build());
    
    public EnhancedToolsModule() {
        super("enhanced_tools", "Enhanced Tools", 
              "Improves vanilla tools with quality-of-life features and adds reinforced variants");
    }
    
    @Override
    protected void onInitialize() {
        // Register our deferred registers
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        
        logger.info("Enhanced Tools module registered {} items", ITEMS.getEntries().size());
    }
    
    @Override
    protected void onCommonSetup() {
        logger.debug("Enhanced Tools module common setup complete");
    }
    
    @Override
    protected void onClientSetup() {
        logger.debug("Enhanced Tools module client setup complete");
    }
}