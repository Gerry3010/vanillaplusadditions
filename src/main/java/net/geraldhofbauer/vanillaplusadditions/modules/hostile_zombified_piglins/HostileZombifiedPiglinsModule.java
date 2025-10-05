package net.geraldhofbauer.vanillaplusadditions.modules.hostile_zombified_piglins;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.geraldhofbauer.vanillaplusadditions.core.AbstractModuleConfig;
import net.geraldhofbauer.vanillaplusadditions.core.ModuleConfig;
import net.geraldhofbauer.vanillaplusadditions.modules.hostile_zombified_piglins.config.HostileZombifiedPiglinsConfig;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.lang.reflect.InvocationTargetException;

/**
 * Hostile Zombified Piglins Module
 * <p>
 * Makes zombified piglins always aggressive towards players, removing their
 * neutral behavior. This creates a more challenging Nether experience while
 * maintaining vanilla mechanics.
 * <p>
 * Features:
 * - Zombified piglins spawn already angry at all players
 * - Removes the "forgiveness" mechanic where they calm down over time
 * - Maintains pack behavior where attacking one angers nearby ones
 * - Configurable detection range and anger duration
 */
public class HostileZombifiedPiglinsModule extends AbstractModule<HostileZombifiedPiglinsModule, HostileZombifiedPiglinsConfig> {


    public HostileZombifiedPiglinsModule() {
        super("hostile_zombified_piglins",
                "Hostile Zombified Piglins",
                "Makes zombified piglins always aggressive towards players, creating a more challenging Nether experience",
                HostileZombifiedPiglinsConfig::new
        );
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

        // Get detection range from configuration
        int detectionRange = config.getDetectionRangeValue();

        // Find all players within the configured range
        var level = zombifiedPiglin.level();
        var nearbyPlayers = level.getEntitiesOfClass(Player.class,
                zombifiedPiglin.getBoundingBox().inflate(detectionRange),
                player -> !player.isCreative() && !player.isSpectator());

        // Set the first player as the persistent anger target
        if (!nearbyPlayers.isEmpty()) {
            Player targetPlayer = nearbyPlayers.get(0);
            zombifiedPiglin.setPersistentAngerTarget(targetPlayer.getUUID());

            // Use configured anger duration
            int angerDuration = config.getAngerDurationValue();
            if (angerDuration == -1) {
                // Indefinite anger
                zombifiedPiglin.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
            } else {
                zombifiedPiglin.setRemainingPersistentAngerTime(angerDuration);
            }

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

        // Ensure anger time doesn't decrease naturally (only if configured for indefinite anger)
        int configuredDuration = config.getAngerDurationValue();
        if (configuredDuration == -1 && zombifiedPiglin.getRemainingPersistentAngerTime() < Integer.MAX_VALUE / 2) {
            zombifiedPiglin.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
        }
    }

    /**
     * Helper method to check if this specific module is enabled.
     */
    private boolean isModuleEnabled() {
        // During initialization, assume enabled if isInitialized is true
        // After initialization, check configuration
        if (!isInitialized()) {
            return false;
        }

        try {
            return config.isEnabled();
        } catch (Exception e) {
            // If config not available yet, return true to allow initialization
            logger.debug("Config not available during module enabled check: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public boolean isEnabledByDefault() {
        return true; // Now configurable, so can be enabled by default
    }

    @Override
    public boolean isConfigurable() {
        return true; // Players should be able to enable/disable this challenging feature
    }
}