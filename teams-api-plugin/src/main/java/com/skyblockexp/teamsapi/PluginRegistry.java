package com.skyblockexp.teamsapi;

import java.util.logging.Logger;

/**
 * Central registry that holds shared references for the TeamsAPI plugin.
 *
 * <p>Initialised by {@link PluginBootstrap#start} and cleared by
 * {@link PluginBootstrap#stop}. All fields are package-private so that only
 * classes in this module can read them.</p>
 */
final class PluginRegistry {

    /** Plugin messaging channel identifier for the Velocity bridge. */
    static final String BRIDGE_CHANNEL = "teamsapi:bridge";

    /** The owning plugin instance. Set during {@link PluginBootstrap#start}. */
    private static TeamsApiPlugin plugin;

    /**
     * Private constructor — this class is a static holder; it is never instantiated.
     */
    private PluginRegistry() {
    }

    /**
     * Initialises the registry with the active plugin instance.
     *
     * @param instance the plugin that is starting up
     */
    static void init(final TeamsApiPlugin instance) {
        plugin = instance;
    }

    /**
     * Clears all references held by the registry.
     */
    static void clear() {
        plugin = null;
    }

    /**
     * Returns the active plugin instance.
     *
     * @return the plugin instance, or {@code null} when not yet initialised
     */
    static TeamsApiPlugin getPlugin() {
        return plugin;
    }

    /**
     * Returns the plugin's logger.
     *
     * @return the logger, or {@code null} when not yet initialised
     */
    static Logger getLogger() {
        return plugin == null ? null : plugin.getLogger();
    }

}
