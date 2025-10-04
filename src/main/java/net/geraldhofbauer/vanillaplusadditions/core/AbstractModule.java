package net.geraldhofbauer.vanillaplusadditions.core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for modules that provides common functionality.
 * Extends this class to create new modules with less boilerplate.
 */
public abstract class AbstractModule implements Module {
    protected final Logger logger;
    protected final String moduleId;
    protected final String displayName;
    protected final String description;
    protected final ModuleConfig config;
    
    protected IEventBus modEventBus;
    protected ModContainer modContainer;
    
    /**
     * Creates a new abstract module.
     * 
     * @param moduleId The unique module identifier
     * @param displayName The human-readable module name
     * @param description The module description
     */
    protected AbstractModule(String moduleId, String displayName, String description) {
        this.moduleId = moduleId;
        this.displayName = displayName;
        this.description = description;
        this.config = AbstractModuleConfig.createDefault(this);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Creates a new abstract module.
     *
     * @param moduleId The unique module identifier
     * @param displayName The human-readable module name
     * @param description The module description
     */
    protected AbstractModule(String moduleId, String displayName, String description, ModuleConfig config) {
        this.moduleId = moduleId;
        this.displayName = displayName;
        this.description = description;
        this.config = config;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public String getModuleId() {
        return moduleId;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public void initialize(IEventBus modEventBus, ModContainer modContainer) {
        this.modEventBus = modEventBus;
        this.modContainer = modContainer;
        
        logger.debug("Initializing module: {}", displayName);
        
        // Call the implementation-specific initialization
        onInitialize();
        
        logger.debug("Module initialized: {}", displayName);
    }
    
    /**
     * Override this method to implement module-specific initialization logic.
     * The modEventBus and modContainer fields will be available at this point.
     */
    protected abstract void onInitialize();
    
    @Override
    public void commonSetup() {
        logger.debug("Running common setup for module: {}", displayName);
        onCommonSetup();
    }
    
    /**
     * Override this method to implement common setup logic.
     * This is called after all registries are populated.
     */
    protected void onCommonSetup() {
        // Default empty implementation
    }
    
    @Override
    public void clientSetup() {
        logger.debug("Running client setup for module: {}", displayName);
        onClientSetup();
    }
    
    /**
     * Override this method to implement client-side setup logic.
     * This is only called on the client side.
     */
    protected void onClientSetup() {
        // Default empty implementation
    }
    
    /**
     * Utility method to check if we're in the initialization phase.
     * 
     * @return true if modEventBus is available
     */
    protected boolean isInitialized() {
        return modEventBus != null;
    }
    
    /**
     * Gets the mod event bus. Only available after initialization.
     * 
     * @return The mod event bus
     * @throws IllegalStateException if called before initialization
     */
    protected IEventBus getModEventBus() {
        if (modEventBus == null) {
            throw new IllegalStateException("Mod event bus not available before initialization");
        }
        return modEventBus;
    }
    
    /**
     * Gets the mod container. Only available after initialization.
     * 
     * @return The mod container
     * @throws IllegalStateException if called before initialization
     */
    protected ModContainer getModContainer() {
        if (modContainer == null) {
            throw new IllegalStateException("Mod container not available before initialization");
        }
        return modContainer;
    }

    @Override
    public ModuleConfig getConfig() {
        return config;
    }
}