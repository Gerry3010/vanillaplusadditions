package net.geraldhofbauer.vanillaplusadditions.core;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for module configurations.
 * This class provides the standard "enabled" property that all modules should have,
 * and allows subclasses to add their own specific configuration options.
 */
public abstract class AbstractModuleConfig<M extends Module, C extends ModuleConfig> implements ModuleConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModuleConfig.class);
    
    // Standard configuration value that all modules have
    protected ModConfigSpec.BooleanValue enabled;
    
    // Module reference for getting default values
    private final M module;
    
    /**
     * Creates an abstract module configuration.
     * 
     * @param module The module this configuration belongs to
     */
    protected AbstractModuleConfig(M module) {
        this.module = module;
    }
    
    @Override
    public final void buildConfig(ModConfigSpec.Builder builder) {
        String moduleId = getConfigSectionName();
        String displayName = module.getDisplayName();
        
        builder.comment(String.format("Configuration for %s module", displayName))
               .push(moduleId);
        
        // Always add the enabled property first
        enabled = builder
            .comment(String.format("Whether the %s module is enabled", displayName))
            .define("enabled", module.isEnabledByDefault());
        
        // Allow subclasses to add their own configuration
        buildModuleSpecificConfig(builder);
        
        builder.pop();
        
        LOGGER.debug("Built configuration for module: {}", moduleId);
    }
    
    /**
     * Override this method to add module-specific configuration options.
     * The enabled property is already handled by the base class.
     * 
     * @param builder The config spec builder (already pushed to the module section)
     */
    protected void buildModuleSpecificConfig(ModConfigSpec.Builder builder) {
        // Default implementation - no additional config
    }
    
    @Override
    public void onConfigLoad(ModConfigSpec spec) {
        // Default implementation - subclasses can override if needed
        if (enabled != null) {
            LOGGER.debug("Configuration loaded for module: {} - enabled: {}", 
                        getConfigSectionName(), enabled.get());
        }
    }
    
    @Override
    public String getConfigSectionName() {
        return module.getModuleId();
    }
    
    @Override
    public boolean isEnabled() {
        if (enabled == null) {
            // Configuration not built yet, return module default
            LOGGER.debug("Config not built yet for {}, returning default: {}", 
                        module.getModuleId(), module.isEnabledByDefault());
            return module.isEnabledByDefault();
        }
        
        try {
            return enabled.get();
        } catch (Exception e) {
            // Config not loaded yet, return module default
            LOGGER.debug("Config not loaded yet for {}, returning default: {} - {}", 
                        module.getModuleId(), module.isEnabledByDefault(), e.getMessage());
            return module.isEnabledByDefault();
        }
    }
    
    /**
     * Gets the enabled configuration value.
     * This is protected to allow subclasses to access it if needed.
     * 
     * @return The enabled configuration value
     */
    protected ModConfigSpec.BooleanValue getEnabledConfig() {
        return enabled;
    }
    
    /**
     * Gets the module this configuration belongs to.
     * This is protected to allow subclasses to access module information.
     * 
     * @return The module instance
     */
    public M getModule() {
        return module;
    }
    
    /**
     * Checks if debug logging should be enabled for this module.
     * This combines global and module-specific debug logging settings.
     * Module-specific settings override the global setting.
     * 
     * @return true if debug logging should be enabled
     */
    public boolean shouldDebugLog() {
        // Check if global debug logging is enabled
        boolean globalDebug = ModulesConfig.isGlobalDebugLoggingEnabled();
        
        // Module-specific debug logging can override the global setting
        // Subclasses should override this method if they have module-specific debug logging
        return globalDebug;
    }
    
    /**
     * Creates a default module configuration instance that only provides
     * the standard enabled/disabled functionality.
     * <p>
     * This is useful for modules that don't need any custom configuration
     * options beyond the basic enable/disable control.
     * 
     * @param module The module this configuration belongs to
     * @return A default configuration instance with only enabled/disabled functionality
     */
    public static <MO extends Module> DefaultModuleConfig<MO> createDefault(MO module) {
        return new DefaultModuleConfig<>(module);
    }

    public static class DefaultModuleConfig<MO extends Module> extends AbstractModuleConfig<MO, DefaultModuleConfig<MO>> {
        DefaultModuleConfig(MO module) {
            super(module);
        }
        // Keine zusätzliche Konfiguration erforderlich
    }
}
