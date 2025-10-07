package net.geraldhofbauer.vanillaplusadditions.modules.wither_skeleton;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.geraldhofbauer.vanillaplusadditions.core.AbstractModuleConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Wither Skeleton Module
 * <p>
 * Prevents normal skeletons from spawning in the Nether and broadcasts a message
 * when this happens. Optionally replaces them with Wither Skeletons to maintain
 * the intended Nether difficulty.
 * <p>
 * Features:
 * - Prevents normal skeleton spawns in the Nether
 * - Broadcasts configurable messages to all players
 * - Option to replace blocked skeletons with Wither Skeletons
 * - Configurable message format and replacement behavior
 */
public class WitherSkeletonModule
        extends AbstractModule<WitherSkeletonModule, AbstractModuleConfig.DefaultModuleConfig<WitherSkeletonModule>> {

    public WitherSkeletonModule() {
        super("wither_skeleton",
                "Wither Skeleton Enforcer",
                "Prevents normal skeletons from spawning in the Nether and broadcasts messages "
                        + "about blocked spawns",
                AbstractModuleConfig::createDefault
        );
    }

    @Override
    protected void onInitialize() {
        // Register event listeners for this module
        NeoForge.EVENT_BUS.register(this);

        getLogger().info("Wither Skeleton module initialized - Normal skeletons are now banned from the Nether!");
    }

    @Override
    protected void onCommonSetup() {
        if (getConfig().shouldDebugLog()) {
            getLogger().debug("Wither Skeleton module common setup complete");
        }
    }

    /**
     * Event handler that prevents normal skeleton spawns in the Nether and broadcasts messages
     * Uses HIGH priority to ensure we can cancel the spawn before other mods process it
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntitySpawn(FinalizeSpawnEvent event) {
        if (!isModuleEnabled()) {
            return;
        }

        // Only process on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Check if the entity is a normal skeleton
        if (!(event.getEntity() instanceof Skeleton skeleton)) {
            return;
        }

        // Check if we're in the Nether (cast to ServerLevel to access dimension())
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        ResourceKey<Level> dimension = serverLevel.dimension();
        if (dimension != Level.NETHER) {
            return;
        }

        // This is a normal skeleton trying to spawn in the Nether - block it!
        if (getConfig().shouldDebugLog()) {
            getLogger().debug("Blocked normal skeleton spawn in Nether at {}", event.getEntity().blockPosition());
        }

        // Cancel the spawn
        event.setSpawnCancelled(true);

        // Broadcast message to all players
        broadcastSkeletonBlockedMessage(serverLevel, event.getEntity().blockPosition());

        // Optionally spawn a Wither Skeleton in its place
        replaceWithWitherSkeleton(serverLevel, skeleton, event.getSpawnType());
    }

    /**
     * Broadcasts a message to all players about the blocked skeleton spawn
     */
    private void broadcastSkeletonBlockedMessage(ServerLevel level, BlockPos position) {
        Component message = Component.literal("🔥 A normal skeleton tried to spawn in the Nether but was blocked! 🔥")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.literal("\nLocation: " + position.getX() + ", " + position.getY()
                                + ", " + position.getZ())
                        .withStyle(ChatFormatting.YELLOW));

        // Send to all players on the server
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }

        if (getConfig().shouldDebugLog()) {
            getLogger().info("Broadcasted skeleton block message for spawn at {}", position);
        }
    }

    /**
     * Replaces the blocked skeleton with a Wither Skeleton
     */
    private void replaceWithWitherSkeleton(ServerLevel level, Skeleton originalSkeleton, MobSpawnType spawnType) {
        try {
            // Create a new Wither Skeleton at the same position
            WitherSkeleton witherSkeleton = EntityType.WITHER_SKELETON.create(level);
            if (witherSkeleton == null) {
                getLogger().warn("Failed to create Wither Skeleton replacement");
                return;
            }

            // Copy position and rotation from the original skeleton
            witherSkeleton.moveTo(originalSkeleton.getX(), originalSkeleton.getY(), originalSkeleton.getZ(),
                    originalSkeleton.getYRot(), originalSkeleton.getXRot());

            // Finalize the spawn with the same spawn type
            witherSkeleton.finalizeSpawn(level, level.getCurrentDifficultyAt(witherSkeleton.blockPosition()),
                    spawnType, null);

            // Add the Wither Skeleton to the world
            level.addFreshEntity(witherSkeleton);

            if (getConfig().shouldDebugLog()) {
                getLogger().debug("Replaced blocked skeleton with Wither Skeleton at {}",
                        witherSkeleton.blockPosition());
            }

        } catch (Exception e) {
            getLogger().error("Failed to replace skeleton with Wither Skeleton", e);
        }
    }
}