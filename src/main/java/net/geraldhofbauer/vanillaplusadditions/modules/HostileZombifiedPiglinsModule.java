package net.geraldhofbauer.vanillaplusadditions.modules;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Hostile Zombified Piglins Module
 * 
 * Makes zombified piglins always aggressive towards players, removing their
 * neutral behavior. This creates a more challenging Nether experience while
 * maintaining vanilla mechanics.
 * 
 * Features:
 * - Zombified piglins spawn already angry at all players
 * - Removes the "forgiveness" mechanic where they calm down over time
 * - Maintains pack behavior where attacking one angers nearby ones
 */
public class HostileZombifiedPiglinsModule extends AbstractModule {
    
    public HostileZombifiedPiglinsModule() {
        super("hostile_zombified_piglins", "Hostile Zombified Piglins", 
              "Makes zombified piglins always aggressive towards players, creating a more challenging Nether experience");
    }
    
    @Override
    protected void onInitialize() {
        // Register event listeners for this module
        NeoForge.EVENT_BUS.register(this);
        
        logger.info("Hostile Zombified Piglins module initialized - Nether just got more dangerous!");
    }
    
    @Override
    protected void onCommonSetup() {
        logger.debug("Hostile Zombified Piglins module common setup complete");
    }
    
    /**
     * Event handler that makes zombified piglins aggressive when they spawn
     */
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!isModuleEnabled()) {
            return;
        }
        
        // Check if the entity is a zombified piglin
        if (event.getEntity() instanceof ZombifiedPiglin zombifiedPiglin) {
            // Make it angry at all nearby players immediately
            makeHostileToAllPlayers(zombifiedPiglin);
            
            logger.debug("Made zombified piglin hostile at spawn: {}", zombifiedPiglin.getUUID());
        }
    }
    
    /**
     * Event handler that maintains hostility over time
     */
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Pre event) {
        if (!isModuleEnabled()) {
            return;
        }
        
        // Only process every 20 ticks (1 second) to avoid performance issues
        if (event.getEntity().tickCount % 20 != 0) {
            return;
        }
        
        if (event.getEntity() instanceof ZombifiedPiglin zombifiedPiglin) {
            // Ensure it stays angry and doesn't calm down
            maintainHostility(zombifiedPiglin);
        }
    }
    
    /**
     * Makes a zombified piglin hostile to all players in the area
     */
    private void makeHostileToAllPlayers(ZombifiedPiglin zombifiedPiglin) {
        if (zombifiedPiglin.level().isClientSide) {
            return; // Only process on server side
        }
        
        // Find all players within a reasonable range (32 blocks)
        var level = zombifiedPiglin.level();
        var nearbyPlayers = level.getEntitiesOfClass(Player.class, 
            zombifiedPiglin.getBoundingBox().inflate(32.0),
            player -> !player.isCreative() && !player.isSpectator());
        
        // Set the first player as the persistent anger target
        if (!nearbyPlayers.isEmpty()) {
            Player targetPlayer = nearbyPlayers.get(0);
            zombifiedPiglin.setPersistentAngerTarget(targetPlayer.getUUID());
            zombifiedPiglin.setRemainingPersistentAngerTime(Integer.MAX_VALUE); // Stay angry indefinitely
            zombifiedPiglin.startPersistentAngerTimer();
        }
    }
    
    /**
     * Maintains the zombified piglin's hostility over time
     */
    private void maintainHostility(ZombifiedPiglin zombifiedPiglin) {
        if (zombifiedPiglin.level().isClientSide) {
            return; // Only process on server side
        }
        
        // If the piglin isn't angry, find a nearby player to be angry at
        if (!zombifiedPiglin.isAngryAt(null) || zombifiedPiglin.getRemainingPersistentAngerTime() < 100) {
            makeHostileToAllPlayers(zombifiedPiglin);
        }
        
        // Ensure anger time doesn't decrease naturally
        if (zombifiedPiglin.getRemainingPersistentAngerTime() < Integer.MAX_VALUE / 2) {
            zombifiedPiglin.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
        }
    }
    
    /**
     * Helper method to check if this specific module is enabled.
     */
    private boolean isModuleEnabled() {
        // In a full implementation, this would check the configuration
        // For now, we'll assume it's enabled if the module was initialized
        return isInitialized();
    }
    
    @Override
    public boolean isEnabledByDefault() {
        // This significantly changes gameplay, so disabled by default
        return false;
    }
    
    @Override
    public boolean isConfigurable() {
        return true; // Players should be able to enable/disable this challenging feature
    }
}