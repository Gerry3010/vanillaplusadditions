package net.geraldhofbauer.vanillaplusadditions.modules.hostile_zombified_piglins;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.geraldhofbauer.vanillaplusadditions.modules.hostile_zombified_piglins.config.HostileZombifiedPiglinsConfig;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

    // Do we need to track which piglins are angry at which players? If we find a threshold after which to "switch" targets, we might.
    // For now, just keep them angry at the nearest player.
    // 2 mins later: We store the timestamp of when the player was the nearest, and after that time, we can switch to a new nearest player.
    // This way, if a player is just passing by, the piglin won't switch targets immediately.
    // However, if the player stays in range, the piglin will eventually switch to the new nearest player.
    // This creates a more dynamic and challenging experience.
    protected HashMap<UUID, NearestPlayerTime> angryPiglins = new HashMap<>(); // Maps zombified piglin UUIDs to the player UUID they are angry at

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
        if (isModuleEnabled()) {
            return;
        }

        // Check if the entity is a zombified piglin
        if (event.getEntity() instanceof ZombifiedPiglin zombifiedPiglin) {
            // Make it angry at all nearby players immediately
            angryPiglins.put(zombifiedPiglin.getUUID(), makeHostileToPlayer(zombifiedPiglin, null));

            logger.debug("Made zombified piglin hostile at spawn: {}", zombifiedPiglin.getUUID());
        }
    }

    /**
     * Event handler that maintains hostility over time
     */
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Pre event) {
        if (isModuleEnabled()) {
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
     * Makes a zombified piglin hostile to all players in the area and returns the nearest player (the one that was targeted).
     *
     * @return The nearest player that was targeted, or null if no players are nearby
     */
    private NearestPlayerTime makeHostileToPlayer(ZombifiedPiglin zombifiedPiglin, @Nullable Player specificPlayer) {
        Player targetPlayer = null;

        if (specificPlayer == null) {
            var nearbyPlayers = getNearbyPlayers(zombifiedPiglin);
            if (nearbyPlayers == null) return null;
            // Set the first player as the persistent anger target
            this.logger.debug("Found {} nearby players for zombified piglin {}", nearbyPlayers.size(), zombifiedPiglin.getUUID());
            if (nearbyPlayers.isEmpty()) {
                // No players nearby, clear anger
                zombifiedPiglin.setRemainingPersistentAngerTime(0);
                zombifiedPiglin.setPersistentAngerTarget(null);
                return null;
            }

            targetPlayer = nearbyPlayers.getFirst();
        } else {
            targetPlayer = specificPlayer;
        }

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

        return new NearestPlayerTime(targetPlayer, System.currentTimeMillis());
    }

    private @Nullable List<Player> getNearbyPlayers(ZombifiedPiglin zombifiedPiglin) {
        if (zombifiedPiglin.level().isClientSide) {
            return null;
        }

        // Get detection range from configuration
        int detectionRange = config.getDetectionRangeValue();

        // Find all players within the configured range
        var level = zombifiedPiglin.level();
        var nearbyPlayers = level.getEntitiesOfClass(Player.class,
                zombifiedPiglin.getBoundingBox().inflate(detectionRange),
                player -> !player.isCreative() && !player.isSpectator());
        return nearbyPlayers;
    }

    /**
     * Maintains the zombified piglin's hostility over time
     */
    private void maintainHostility(ZombifiedPiglin zombifiedPiglin) {
        if (zombifiedPiglin.level().isClientSide) {
            return; // Only process on server side
        }

        // If the piglin isn't angry, find a nearby player to be angry at

        var nearbyPlayers = getNearbyPlayers(zombifiedPiglin);
        if (nearbyPlayers == null) return;
        Player nearestPlayer = nearbyPlayers.isEmpty() ? null : nearbyPlayers.getFirst();
        if (nearestPlayer == null) {
            // No players nearby, clear anger
            angryPiglins.remove(zombifiedPiglin.getUUID());
            zombifiedPiglin.setRemainingPersistentAngerTime(0);
            zombifiedPiglin.setPersistentAngerTarget(null);
            logger.debug("Zombified piglin {} calmed down (no players nearby)", zombifiedPiglin.getUUID());
            return;
        }

        // Check if we need to switch anger target
        NearestPlayerTime currentTarget = angryPiglins.get(zombifiedPiglin.getUUID());
        Player newTarget = currentTarget == null ? nearestPlayer : currentTarget.player();
        long newTimeStamp = currentTarget == null ? System.currentTimeMillis() : currentTarget.timeStamp();
        // If the nearest player is different from the current target, check if we can switch
        if (currentTarget == null || !currentTarget.player().getUUID().equals(nearestPlayer.getUUID())) {
            // New nearest player, check if we can switch
            if (currentTarget == null || System.currentTimeMillis() - currentTarget.timeStamp() > config.getTargetSwitchThresholdValue(true)) { // 10 seconds threshold
                newTarget = nearestPlayer;
                logger.debug("Zombified piglin {} switching anger target to player {}", zombifiedPiglin.getUUID(), newTarget.getUUID());
            }
        } else if (currentTarget.player().getUUID().equals(nearestPlayer.getUUID())) {
            // Same player, update timestamp
            newTarget = currentTarget.player();
            newTimeStamp = System.currentTimeMillis();
        }
        // Update the map
        angryPiglins.put(zombifiedPiglin.getUUID(), new NearestPlayerTime(newTarget, newTimeStamp));

        if (!zombifiedPiglin.isAngryAt(newTarget) || zombifiedPiglin.getRemainingPersistentAngerTime() < 100) {
            var targetPlayerTime = makeHostileToPlayer(zombifiedPiglin, newTarget);
            if (targetPlayerTime != null) {
                logger.debug("Zombified piglin {} re-angered at player {}", zombifiedPiglin.getUUID(), targetPlayerTime.player().getUUID());
//                angryPiglins.put(zombifiedPiglin.getUUID(), targetPlayerTime);
            } else {
                // No players nearby, clear anger
                angryPiglins.remove(zombifiedPiglin.getUUID());
                zombifiedPiglin.setRemainingPersistentAngerTime(0);
                zombifiedPiglin.setPersistentAngerTarget(null);
                logger.debug("Zombified piglin {} calmed down (no players nearby)", zombifiedPiglin.getUUID());
            }
        }

        // Ensure anger time doesn't decrease naturally (only if configured for indefinite anger)
        int configuredDuration = config.getAngerDurationValue();
        if (configuredDuration == -1 && zombifiedPiglin.getRemainingPersistentAngerTime() < Integer.MAX_VALUE / 2) {
            zombifiedPiglin.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
        }
    }
}