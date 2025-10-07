package net.geraldhofbauer.vanillaplusadditions.modules.mob_glow.config;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModuleConfig;
import net.geraldhofbauer.vanillaplusadditions.core.Module;
import net.geraldhofbauer.vanillaplusadditions.modules.mob_glow.MobGlowModule;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for the Mob Glow module.
 * This class handles all configuration options for the /mobglow command functionality.
 */
public class MobGlowConfig extends AbstractModuleConfig<MobGlowModule, MobGlowConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MobGlowConfig.class);

    // Module-specific configuration values - enabled and debugLogging are handled by AbstractModuleConfig
    private ModConfigSpec.IntValue defaultDuration;
    private ModConfigSpec.IntValue maxDuration;
    private ModConfigSpec.BooleanValue requireOp;
    private ModConfigSpec.IntValue maxMobsPerCommand;

    /**
     * Creates a new MobGlowConfig.
     *
     * @param module The module this configuration belongs to
     */
    public MobGlowConfig(MobGlowModule module) {
        super(module);
    }

    @Override
    protected void buildModuleSpecificConfig(ModConfigSpec.Builder builder) {
        defaultDuration = builder
                .comment("Default duration in seconds for glow effect when 'infinite' is specified (-1 for truly infinite)")
                .defineInRange("default_duration", -1, -1, Integer.MAX_VALUE);

        maxDuration = builder
                .comment("Maximum allowed duration in seconds for glow effect (0 for no limit)")
                .defineInRange("max_duration", 3600, 0, Integer.MAX_VALUE);

        requireOp = builder
                .comment("Whether the command requires operator permissions")
                .define("require_op", true);

        maxMobsPerCommand = builder
                .comment("Maximum number of mobs that can be affected per command (0 for no limit)")
                .defineInRange("max_mobs_per_command", 100, 0, Integer.MAX_VALUE);

        if (shouldDebugLog()) {
            LOGGER.debug("Built module-specific configuration for Mob Glow module");
        }
    }

    @Override
    public void onConfigLoad(ModConfigSpec spec) {
        super.onConfigLoad(spec); // Call parent to handle enabled logging
        // React to module-specific configuration changes if needed
        if (shouldDebugLog()) {
            LOGGER.debug("Module-specific configuration loaded for Mob Glow module");
            if (defaultDuration != null && maxDuration != null && requireOp != null && maxMobsPerCommand != null) {
                LOGGER.debug("  - Default duration: {} seconds", defaultDuration.get());
                LOGGER.debug("  - Max duration: {} seconds", maxDuration.get());
                LOGGER.debug("  - Require OP: {}", requireOp.get());
                LOGGER.debug("  - Max mobs per command: {}", maxMobsPerCommand.get());
            }
        }
    }

    /**
     * Gets the configured default duration.
     *
     * @return default duration in seconds, or default value if not configured
     */
    public int getDefaultDurationValue() {
        return defaultDuration != null ? defaultDuration.get() : -1;
    }

    /**
     * Gets the configured maximum duration.
     *
     * @return maximum duration in seconds, or default value if not configured
     */
    public int getMaxDurationValue() {
        return maxDuration != null ? maxDuration.get() : 3600;
    }

    /**
     * Gets the configured require OP setting.
     *
     * @return true if OP is required, or default value if not configured
     */
    public boolean getRequireOpValue() {
        return requireOp != null ? requireOp.get() : true;
    }

    /**
     * Gets the configured maximum mobs per command.
     *
     * @return maximum mobs per command, or default value if not configured
     */
    public int getMaxMobsPerCommandValue() {
        return maxMobsPerCommand != null ? maxMobsPerCommand.get() : 100;
    }

    /**
     * Gets the default duration configuration value.
     *
     * @return The default duration configuration value
     */
    public ModConfigSpec.IntValue getDefaultDuration() {
        return defaultDuration;
    }

    /**
     * Gets the max duration configuration value.
     *
     * @return The max duration configuration value
     */
    public ModConfigSpec.IntValue getMaxDuration() {
        return maxDuration;
    }

    /**
     * Gets the require OP configuration value.
     *
     * @return The require OP configuration value
     */
    public ModConfigSpec.BooleanValue getRequireOp() {
        return requireOp;
    }

    /**
     * Gets the max mobs per command configuration value.
     *
     * @return The max mobs per command configuration value
     */
    public ModConfigSpec.IntValue getMaxMobsPerCommand() {
        return maxMobsPerCommand;
    }
}