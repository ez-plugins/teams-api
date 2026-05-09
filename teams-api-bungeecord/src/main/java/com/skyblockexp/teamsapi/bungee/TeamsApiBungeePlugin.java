package com.skyblockexp.teamsapi.bungee;

import com.skyblockexp.teamsapi.bungee.api.BungeeTeamsAPI;
import com.skyblockexp.teamsapi.bungee.bridge.BridgeProtocol;
import com.skyblockexp.teamsapi.bungee.bridge.BungeeTeamsServiceImpl;
import com.skyblockexp.teamsapi.bungee.config.ProxyConfig;
import com.skyblockexp.teamsapi.bungee.redis.RedisManager;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * Entry point for the TeamsAPI BungeeCord plugin.
 *
 * <p>Registers the {@code teamsapi:bridge} plugin-messaging channel on startup and
 * provides {@link BungeeTeamsAPI} to any proxy-side plugin that needs team data
 * from connected backend servers.</p>
 *
 * <p>When Redis is enabled in {@code config.yml}, this plugin participates in a
 * multi-proxy setup: queries that cannot be routed locally (because the target player
 * is on a different proxy) are forwarded through Redis Pub/Sub so that the correct
 * proxy can fulfil them.</p>
 *
 * <p>Server owners install this JAR in the BungeeCord {@code plugins/} directory
 * alongside their backend Bukkit/Paper server running the standard TeamsAPI plugin.</p>
 */
public final class TeamsApiBungeePlugin extends Plugin {

    /** The service implementation, available after {@link #onEnable()}. */
    private BungeeTeamsServiceImpl serviceImpl;

    /** Redis manager; null when Redis is disabled. */
    private RedisManager redisManager;

    /**
     * Called when the plugin starts. Registers the bridge channel and initialises
     * the {@link BungeeTeamsAPI} service.
     */
    @Override
    public void onEnable() {
        final ProxyConfig config = ProxyConfig.load(
            getDataFolder(), getResourceAsStream("config.yml"));

        getProxy().registerChannel(BridgeProtocol.CHANNEL_NAME);
        serviceImpl = new BungeeTeamsServiceImpl(getProxy(), config.getQueryTimeoutSeconds());

        if (config.isRedisEnabled()) {
            redisManager = new RedisManager(config, getLogger(),
                (op, args) -> serviceImpl.getDispatcher().queryAny(op, args));
            serviceImpl.getDispatcher().setRedisManager(redisManager);
            redisManager.start();
        }
        else {
            getLogger().info("TeamsAPI Redis multi-proxy bridge is disabled."
                + " Single-proxy mode active.");
        }

        getProxy().getPluginManager().registerListener(this, serviceImpl.getDispatcher());
        BungeeTeamsAPI.setService(serviceImpl);
        getLogger().info("TeamsAPI BungeeCord bridge ready. Channel: " + BridgeProtocol.CHANNEL_NAME);
    }

    /**
     * Called when the plugin stops. Clears the registered service and unregisters the channel.
     */
    @Override
    public void onDisable() {
        BungeeTeamsAPI.clearService();
        if (redisManager != null) {
            redisManager.shutdown();
            redisManager = null;
        }
        getProxy().unregisterChannel(BridgeProtocol.CHANNEL_NAME);
        getLogger().info("TeamsAPI BungeeCord bridge disabled.");
    }
}
