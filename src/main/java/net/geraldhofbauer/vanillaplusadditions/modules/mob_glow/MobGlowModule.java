package net.geraldhofbauer.vanillaplusadditions.modules.mob_glow;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.geraldhofbauer.vanillaplusadditions.core.AbstractModule;
import net.geraldhofbauer.vanillaplusadditions.modules.mob_glow.config.MobGlowConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mob Glow Module
 * <p>
 * Provides a command to make all mobs of a specified type glow for a given duration.
 * This is useful for server administrators and operators to highlight specific mob types
 * for debugging, demonstration, or gameplay purposes.
 * <p>
 * Features:
 * - /mobglow command with entity type and duration parameters
 * - OP-only command (configurable)
 * - Support for infinite duration or specific time limits
 * - Configurable limits on affected mobs and duration
 * - Auto-completion for entity types
 */
public class MobGlowModule extends AbstractModule<MobGlowModule, MobGlowConfig> {

    // Suggestion provider for entity types
    private static final SuggestionProvider<CommandSourceStack> ENTITY_TYPE_SUGGESTIONS = (context, builder) -> {
        return SharedSuggestionProvider.suggestResource(
                BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                        .filter(resourceLocation -> BuiltInRegistries.ENTITY_TYPE.get(resourceLocation) != EntityType.PLAYER), // Exclude players
                builder
        );
    };

    public MobGlowModule() {
        super("mob_glow",
                "Mob Glow Command",
                "Provides a command to make all mobs of a specified type glow for debugging and visualization",
                MobGlowConfig::new
        );
    }

    @Override
    protected void onInitialize() {
        // Register event listeners for this module
        NeoForge.EVENT_BUS.register(this);

        logger.info("Mob Glow module initialized - /mobglow command ready!");
    }

    @Override
    protected void onCommonSetup() {
        if (config.shouldDebugLog()) {
            logger.debug("Mob Glow module common setup complete");
        }
    }

    /**
     * Register the /mobglow command when commands are being registered
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        if (isModuleEnabled()) {
            return;
        }

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        if (config.shouldDebugLog()) {
            logger.debug("Registering /mobglow command");
        }

        dispatcher.register(
                Commands.literal("mobglow")
                        .requires(source -> !config.getRequireOpValue() || source.hasPermission(2)) // OP level 2 required by default
                        .then(Commands.argument("entity_type", ResourceLocationArgument.id())
                                .suggests(ENTITY_TYPE_SUGGESTIONS)
                                .executes(this::executeMobGlowInfinite) // Default to infinite
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .executes(this::executeMobGlowWithDuration)
                                )
                        )
        );

        if (config.shouldDebugLog()) {
            logger.debug("Successfully registered /mobglow command");
        }
    }

    /**
     * Execute mobglow command with infinite duration (default)
     */
    private int executeMobGlowInfinite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation entityTypeId = ResourceLocationArgument.getId(context, "entity_type");
        return executeMobGlow(context, entityTypeId, "infinite");
    }

    /**
     * Execute mobglow command with specified duration
     */
    private int executeMobGlowWithDuration(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation entityTypeId = ResourceLocationArgument.getId(context, "entity_type");
        String durationStr = StringArgumentType.getString(context, "duration");
        return executeMobGlow(context, entityTypeId, durationStr);
    }

    /**
     * Main execution logic for the mobglow command
     */
    private int executeMobGlow(CommandContext<CommandSourceStack> context, ResourceLocation entityTypeId, String durationStr) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        
        // Validate entity type
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityTypeId)) {
            source.sendFailure(Component.literal("Unknown entity type: " + entityTypeId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityTypeId);
        if (entityType == EntityType.PLAYER) {
            source.sendFailure(Component.literal("Cannot apply glow effect to players")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Parse duration
        int durationTicks;
        boolean isInfinite = false;
        
        if ("infinite".equalsIgnoreCase(durationStr)) {
            int configDefault = config.getDefaultDurationValue();
            if (configDefault == -1) {
                durationTicks = Integer.MAX_VALUE;
                isInfinite = true;
            } else {
                durationTicks = configDefault * 20; // Convert seconds to ticks
            }
        } else {
            try {
                int durationSeconds = Integer.parseInt(durationStr);
                
                // Check maximum duration limit
                int maxDuration = config.getMaxDurationValue();
                if (maxDuration > 0 && durationSeconds > maxDuration) {
                    source.sendFailure(Component.literal("Duration cannot exceed " + maxDuration + " seconds")
                            .withStyle(ChatFormatting.RED));
                    return 0;
                }
                
                durationTicks = durationSeconds * 20; // Convert seconds to ticks
            } catch (NumberFormatException e) {
                source.sendFailure(Component.literal("Invalid duration: " + durationStr + ". Use a number or 'infinite'")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        if (config.shouldDebugLog()) {
            logger.debug("Executing /mobglow command: entity_type={}, duration={} ({}ticks, infinite={})", 
                    entityTypeId, durationStr, durationTicks, isInfinite);
        }

        // Apply glow effect to all matching entities
        AtomicInteger affectedCount = new AtomicInteger(0);
        int maxMobs = config.getMaxMobsPerCommandValue();
        
        if (source.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getAllEntities().forEach(entity -> {
                if (maxMobs > 0 && affectedCount.get() >= maxMobs) {
                    return; // Skip if we've reached the limit
                }
                
                if (entity.getType() == entityType && entity instanceof LivingEntity livingEntity) {
                    // Apply glowing effect
                    MobEffectInstance glowEffect = new MobEffectInstance(MobEffects.GLOWING, durationTicks, 0, false, true);
                    livingEntity.addEffect(glowEffect);
                    affectedCount.incrementAndGet();
                    
                    if (config.shouldDebugLog()) {
                        logger.debug("Applied glow effect to {} at {}", entityType.getDescriptionId(), entity.blockPosition());
                    }
                }
            });
        }

        // Send result message
        if (affectedCount.get() == 0) {
            source.sendSuccess(() -> Component.literal("No " + entityTypeId + " entities found to apply glow effect")
                    .withStyle(ChatFormatting.YELLOW), true);
        } else {
            String durationText = isInfinite ? "indefinitely" : "for " + (durationTicks / 20) + " seconds";
            source.sendSuccess(() -> Component.literal("Applied glow effect to " + affectedCount.get() + " " + entityTypeId + " entities " + durationText)
                    .withStyle(ChatFormatting.GREEN), true);
            
            if (config.shouldDebugLog()) {
                logger.debug("Successfully applied glow effect to {} entities of type {}", affectedCount.get(), entityTypeId);
            }
        }

        return affectedCount.get();
    }
}