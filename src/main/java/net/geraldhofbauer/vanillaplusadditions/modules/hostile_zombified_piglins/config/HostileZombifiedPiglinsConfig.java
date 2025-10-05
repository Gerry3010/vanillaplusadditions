package net.geraldhofbauer.vanillaplusadditions.modules.hostile_zombified_piglins.config;

import net.geraldhofbauer.vanillaplusadditions.core.AbstractModuleConfig;
import net.geraldhofbauer.vanillaplusadditions.core.Module;
import net.geraldhofbauer.vanillaplusadditions.modules.hostile_zombified_piglins.HostileZombifiedPiglinsModule;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for the Hostile Zombified Piglins module.
 * This class handles all configuration options specific to making zombified piglins hostile.
 */
public class HostileZombifiedPiglinsConfig extends AbstractModuleConfig<HostileZombifiedPiglinsModule, HostileZombifiedPiglinsConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostileZombifiedPiglinsConfig.class);

    // Module-specific configuration values - enabled is handled by AbstractModuleConfig
    private ModConfigSpec.IntValue detectionRange;
    private ModConfigSpec.IntValue angerDuration;
    private ModConfigSpec.DoubleValue targetSwitchThreshold;

    /**
     * Creates a new HostileZombifiedPiglinsConfig.
     *
     * @param module The module this configuration belongs to
     */
    public HostileZombifiedPiglinsConfig(HostileZombifiedPiglinsModule module) {
        super(module);
    }


    /**
     * Gets the detection range configuration value.
     *
     * @return The detection range configuration value
     */
    public ModConfigSpec.IntValue getDetectionRange() {
        return detectionRange;
    }

    /**
     * Gets the anger duration configuration value.
     *
     * @return The anger duration configuration value
     */
    public ModConfigSpec.IntValue getAngerDuration() {
        return angerDuration;
    }

    /**
     * Gets the target switch threshold configuration value. (In seconds)
     *
     * @return The target switch threshold configuration value
     */
    public ModConfigSpec.DoubleValue getTargetSwitchThreshold() { return targetSwitchThreshold; }

    @Override
    protected void buildModuleSpecificConfig(ModConfigSpec.Builder builder) {
        detectionRange = builder
                .comment("Range in blocks to detect players and become hostile")
                .defineInRange("detection_range", 32, 1, 128);

        angerDuration = builder
                .comment("How long zombified piglins stay angry in ticks (-1 for indefinite)")
                .defineInRange("anger_duration", -1, -1, Integer.MAX_VALUE);

        targetSwitchThreshold = builder
                .comment("Time in seconds before a zombified piglin can switch to a new nearest player target")
                .defineInRange("target_switch_threshold", 5.0, 0.0, Double.MAX_VALUE);

        LOGGER.debug("Built module-specific configuration for Hostile Zombified Piglins module");
    }

    @Override
    public void onConfigLoad(ModConfigSpec spec) {
        super.onConfigLoad(spec); // Call parent to handle enabled logging
        // React to module-specific configuration changes if needed
        LOGGER.debug("Module-specific configuration loaded for Hostile Zombified Piglins module");
        if (detectionRange != null && angerDuration != null) {
            LOGGER.debug("  - Detection range: {} blocks", detectionRange.get());
            LOGGER.debug("  - Anger duration: {} ticks", angerDuration.get());
            LOGGER.debug("  - Target switch threshold: {} seconds", targetSwitchThreshold.get());
        }
    }


    /**
     * Gets the configured detection range.
     *
     * @return detection range in blocks, or default value if not configured
     */
    public int getDetectionRangeValue() {
        return detectionRange != null ? detectionRange.get() : 32;
    }

    /**
     * Gets the configured anger duration.
     *
     * @return anger duration in ticks, or default value if not configured
     */
    public int getAngerDurationValue() {
        return angerDuration != null ? angerDuration.get() : -1;
    }

    /**
     * Gets the configured target switch threshold.
     * If convertToMillis is true, the value is returned in milliseconds.
     *
     * @return target switch threshold in seconds/milliseconds, or default value if not configured
     */
    public double getTargetSwitchThresholdValue(boolean convertToMillis) {
        double seconds = targetSwitchThreshold != null ? targetSwitchThreshold.get() : 5;
        return convertToMillis ? seconds * 1000 : seconds;
    }

    public double getTargetSwitchThresholdValue() {
        return getTargetSwitchThresholdValue(false);
    }
}