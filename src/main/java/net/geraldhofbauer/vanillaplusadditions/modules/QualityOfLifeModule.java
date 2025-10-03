package net.geraldhofbauer.vanillaplusadditions.modules;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Quality of Life Module
 * 
 * Provides various quality-of-life improvements that don't add new content
 * but make the vanilla experience more pleasant:
 * - Better player feedback
 * - Small gameplay improvements
 * - Enhanced user interface elements
 */
public class QualityOfLifeModule extends AbstractModule {
    
    public QualityOfLifeModule() {
        super("quality_of_life", "Quality of Life", 
              "Small improvements that enhance the vanilla Minecraft experience");
    }
    
    @Override
    protected void onInitialize() {
        // Register event listeners for this module
        NeoForge.EVENT_BUS.register(this);
        
        logger.info("Quality of Life module event handlers registered");
    }
    
    @Override
    protected void onCommonSetup() {
        logger.debug("Quality of Life module common setup complete");
    }
    
    @Override
    protected void onClientSetup() {
        logger.debug("Quality of Life module client setup complete");
    }
    
    // Example event handler - welcome message for new players
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!isModuleEnabled()) {
            return;
        }
        
        // This could show a welcome message or provide helpful tips
        logger.debug("Player {} logged in - QoL module is active", event.getEntity().getName().getString());
    }
    
    /**
     * Helper method to check if this specific module is enabled.
     * This allows for fine-grained control over module features.
     */
    private boolean isModuleEnabled() {
        // In a full implementation, this would check the configuration
        // For now, we'll assume it's enabled if the module was initialized
        return isInitialized();
    }
    
    @Override
    public boolean isConfigurable() {
        return true; // This module should definitely be configurable
    }
}