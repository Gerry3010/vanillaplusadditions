package net.geraldhofbauer.vanillaplusadditions.core;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Interface for modules that have configurable options.
 * Modules implementing this interface can define their own configuration settings
 * beyond just being enabled/disabled.
 */
public interface ModuleConfig {
    
    /**
     * Builds the configuration specification for this module.
     * This method is called during the config initialization phase to allow
     * the module to define its configuration options.
     * 
     * @param builder The config spec builder to add configuration options to
     */
    void buildConfig(ModConfigSpec.Builder builder);
    
    /**
     * Called when the module's configuration is loaded or reloaded.
     * Use this to cache configuration values or react to config changes.
     * 
     * @param spec The loaded configuration specification
     */
    default void onConfigLoad(ModConfigSpec spec) {
        // Default empty implementation
    }
    
    /**
     * Gets the configuration section name for this module.
     * Configuration options will be nested under this section.
     * If null, options will be placed at the root level (not recommended for modules).
     * 
     * @return The configuration section name, or null for root level
     */
    default String getConfigSectionName() {
        if (this instanceof Module module) {
            return module.getModuleId();
        }
        return "unknown_module";
    }
    
    /**
     * Checks if the module is enabled according to its configuration.
     * This method should return the current enabled state from the configuration.
     * 
     * @return true if the module is enabled, false otherwise
     */
    default boolean isEnabled() {
        return true; // Default implementation for configs that don't have enabled option
    }
}
