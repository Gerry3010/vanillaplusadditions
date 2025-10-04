package net.geraldhofbauer.vanillaplusadditions.core;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration manager for all VanillaPlusAdditions modules.
 * This class handles the creation and management of configuration options
 * for modules, including the ability to enable/disable them.
 */
@EventBusSubscriber(modid = "vanillaplusadditions", bus = EventBusSubscriber.Bus.MOD)
public class ModulesConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesConfig.class);
    
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    // Module enable/disable configurations
    private static final Map<String, ModConfigSpec.BooleanValue> moduleEnabledConfigs = new HashMap<>();
    
    // Pre-defined module configurations (we need to define these statically)
    public static final ModConfigSpec.BooleanValue HOSTILE_ZOMBIFIED_PIGLINS_ENABLED;
    public static final ModConfigSpec.BooleanValue ENHANCED_TOOLS_ENABLED;
    public static final ModConfigSpec.BooleanValue IMPROVED_STORAGE_ENABLED;
    public static final ModConfigSpec.BooleanValue QUALITY_OF_LIFE_ENABLED;
    
    // HostileZombifiedPiglins module specific config
    public static final ModConfigSpec.IntValue HOSTILE_ZOMBIFIED_PIGLINS_DETECTION_RANGE;
    public static final ModConfigSpec.IntValue HOSTILE_ZOMBIFIED_PIGLINS_ANGER_DURATION;
    
    public static final ModConfigSpec SPEC;
    
    static {
        BUILDER.comment("VanillaPlusAdditions Module Configuration")
               .push("modules");
        
        // Hostile Zombified Piglins Module
        BUILDER.comment("Configuration for Hostile Zombified Piglins module")
               .push("hostile_zombified_piglins");
        
        HOSTILE_ZOMBIFIED_PIGLINS_ENABLED = BUILDER
            .comment("Whether the Hostile Zombified Piglins module is enabled")
            .define("enabled", true);
            
        HOSTILE_ZOMBIFIED_PIGLINS_DETECTION_RANGE = BUILDER
            .comment("Range in blocks to detect players and become hostile")
            .defineInRange("detection_range", 32, 1, 128);
            
        HOSTILE_ZOMBIFIED_PIGLINS_ANGER_DURATION = BUILDER
            .comment("How long zombified piglins stay angry in ticks (-1 for indefinite)")
            .defineInRange("anger_duration", -1, -1, Integer.MAX_VALUE);
            
        BUILDER.pop();
        
        // Enhanced Tools Module
        BUILDER.comment("Configuration for Enhanced Tools module")
               .push("enhanced_tools");
        
        ENHANCED_TOOLS_ENABLED = BUILDER
            .comment("Whether the Enhanced Tools module is enabled")
            .define("enabled", true);
            
        BUILDER.pop();
        
        // Improved Storage Module
        BUILDER.comment("Configuration for Improved Storage module")
               .push("improved_storage");
        
        IMPROVED_STORAGE_ENABLED = BUILDER
            .comment("Whether the Improved Storage module is enabled")
            .define("enabled", true);
            
        BUILDER.pop();
        
        // Quality of Life Module
        BUILDER.comment("Configuration for Quality of Life module")
               .push("quality_of_life");
        
        QUALITY_OF_LIFE_ENABLED = BUILDER
            .comment("Whether the Quality of Life module is enabled")
            .define("enabled", true);
            
        BUILDER.pop();
        
        BUILDER.pop(); // Close modules section
        
        SPEC = BUILDER.build();
        
        // Store the config references for easy lookup
        moduleEnabledConfigs.put("hostile_zombified_piglins", HOSTILE_ZOMBIFIED_PIGLINS_ENABLED);
        moduleEnabledConfigs.put("enhanced_tools", ENHANCED_TOOLS_ENABLED);
        moduleEnabledConfigs.put("improved_storage", IMPROVED_STORAGE_ENABLED);
        moduleEnabledConfigs.put("quality_of_life", QUALITY_OF_LIFE_ENABLED);
    }
    
    /**
     * Gets the configuration value for whether a specific module is enabled.
     * 
     * @param moduleId The module ID
     * @return The configuration value, or null if not found
     */
    public static ModConfigSpec.BooleanValue getModuleEnabledConfig(String moduleId) {
        return moduleEnabledConfigs.get(moduleId);
    }
    
    /**
     * Checks if a module is enabled according to configuration.
     * Falls back to the module's default enabled state if no config exists.
     * 
     * @param module The module to check
     * @return true if the module should be enabled
     */
    public static boolean isModuleEnabled(Module module) {
        String moduleId = module.getModuleId();
        
        // Try to find the config value
        ModConfigSpec.BooleanValue enabledConfig = moduleEnabledConfigs.get(moduleId);
        if (enabledConfig != null) {
            try {
                boolean configValue = enabledConfig.get();
                LOGGER.debug("Module {} enabled state: {} (from config)", moduleId, configValue);
                return configValue;
            } catch (Exception e) {
                LOGGER.debug("Config not yet loaded for module {}, using default: {}", 
                           moduleId, e.getMessage());
            }
        }
        
        // Fall back to module default
        boolean defaultEnabled = module.isEnabledByDefault();
        LOGGER.debug("Module {} enabled state: {} (default, no config found)", moduleId, defaultEnabled);
        return defaultEnabled;
    }
    
    /**
     * Handles module configuration events.
     * Called when configuration is loaded or reloaded.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (!event.getConfig().getModId().equals("vanillaplusadditions")) {
            return;
        }
        
        LOGGER.debug("VanillaPlusAdditions module configuration loaded: {}", event.getConfig().getFileName());
        
        // Notify all configurable modules about the config load
        for (Module module : ModuleManager.getInstance().getAllModules()) {
            if (module instanceof ModuleConfig configurableModule) {
                try {
                    configurableModule.onConfigLoad(SPEC);
                } catch (Exception e) {
                    LOGGER.error("Error loading configuration for module {}: {}", 
                               module.getModuleId(), e.getMessage());
                }
            }
        }
        
        // Log the current module states
        LOGGER.info("Module configuration reloaded. Current states:");
        for (Module module : ModuleManager.getInstance().getAllModules()) {
            boolean enabled = isModuleEnabled(module);
            LOGGER.info("  - {}: {}", module.getModuleId(), enabled ? "ENABLED" : "DISABLED");
        }
    }
}
