package com.skyblockexp.teamsapi.velocity;

import com.skyblockexp.teamsapi.velocity.api.VelocityTeamsAPI;
import com.skyblockexp.teamsapi.velocity.bridge.BridgeProtocol;
import com.skyblockexp.teamsapi.velocity.bridge.VelocityTeamsServiceImpl;
import com.skyblockexp.teamsapi.velocity.config.ProxyConfig;
import com.skyblockexp.teamsapi.velocity.redis.RedisManager;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.nio.file.Path;

import org.slf4j.Logger;

/**
 * Entry point for the TeamsAPI Velocity plugin.
 *
 * <p>Registers the {@code teamsapi:bridge} plugin-messaging channel on startup and
 * provides {@link VelocityTeamsAPI} to any proxy-side plugin that needs team data
 * from connected backend servers.</p>
 *
 * <p>When Redis is enabled in {@code config.yml}, this plugin participates in a
 * multi-proxy setup: queries that cannot be routed locally (because the target player
 * is on a different proxy) are forwarded through Redis Pub/Sub so that the correct
 * proxy can fulfil them.</p>
 *
 * <p>Server owners install this JAR in the Velocity {@code plugins/} directory
 * alongside their backend Bukkit/Paper server running the standard TeamsAPI plugin.</p>
 */
@Plugin(
    id = "teamsapi",
    name = "TeamsAPI",
    version = "1.3.0",
    description = "Universal Teams API bridge for Velocity.",
    authors = {"ez-plugins"})
public final class TeamsApiVelocityPlugin {

    /** Plugin messaging channel for all bridge traffic. */
    static final MinecraftChannelIdentifier CHANNEL =
        MinecraftChannelIdentifier.from(BridgeProtocol.CHANNEL_NAME);

    /** The Velocity proxy server. */
    private final ProxyServer server;

    /** Logger injected by Velocity. */
    private final Logger logger;

    /** Plugin data directory for config storage. */
    private final Path dataDirectory;

    /** The service implementation, available after {@link #onProxyInitialise}. */
    private VelocityTeamsServiceImpl serviceImpl;

    /** Redis manager; null when Redis is disabled. */
    private RedisManager redisManager;

    /**
     * Constructs the plugin. Dependencies are injected by Velocity's Guice injector.
     *
     * @param server        the Velocity proxy server
     * @param logger        the SLF4J logger provided by Velocity
     * @param dataDirectory the plugin data directory
     */
    @Inject
    public TeamsApiVelocityPlugin(final ProxyServer server, final Logger logger,
            @DataDirectory final Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    /**
     * Initialises the bridge channel and service when the proxy starts.
     *
     * @param event the proxy initialisation event
     */
    @Subscribe
    public void onProxyInitialise(final ProxyInitializeEvent event) {
        final ProxyConfig config = ProxyConfig.load(dataDirectory);

        server.getChannelRegistrar().register(CHANNEL);
        serviceImpl = new VelocityTeamsServiceImpl(server, CHANNEL, config.getQueryTimeoutSeconds());

        if (config.isRedisEnabled()) {
            redisManager = new RedisManager(config, logger,
                (op, args) -> serviceImpl.getDispatcher().queryAny(op, args));
            serviceImpl.getDispatcher().setRedisManager(redisManager);
            redisManager.start();
        }
        else {
            logger.info("TeamsAPI Redis multi-proxy bridge is disabled."
                + " Single-proxy mode active.");
        }

        server.getEventManager().register(this, serviceImpl.getDispatcher());
        VelocityTeamsAPI.setService(serviceImpl);
        logger.info("TeamsAPI Velocity bridge enabled on channel {}.", CHANNEL.getId());
    }

    /**
     * Unregisters the bridge channel and clears the service when the proxy shuts down.
     *
     * @param event the proxy shutdown event
     */
    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        VelocityTeamsAPI.clearService();
        if (redisManager != null) {
            redisManager.shutdown();
            redisManager = null;
        }
        server.getChannelRegistrar().unregister(CHANNEL);
        logger.info("TeamsAPI Velocity bridge disabled.");
    }
}
