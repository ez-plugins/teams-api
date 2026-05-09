package com.skyblockexp.teamsapi.bungee.bridge;

import com.skyblockexp.teamsapi.bungee.redis.RedisManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Dispatches query requests to backend servers and resolves responses.
 *
 * <p>Each call to {@link #query(UUID, String, Map)} selects a connected player to route the
 * plugin message through, stores a pending {@link CompletableFuture}, and resolves it
 * when the backend sends a matching response on the {@code teamsapi:bridge} channel.</p>
 *
 * <p>When an optional {@link RedisManager} is configured, queries that cannot be routed
 * locally (no online player on this proxy) are published to Redis so that another proxy
 * in the network can fulfil them. This enables multi-proxy deployments where players are
 * distributed across multiple BungeeCord instances.</p>
 *
 * <p>Futures time out after the configured number of seconds if no response is received.
 * Pending entries are cleaned up automatically on completion or timeout.</p>
 */
public final class BungeeQueryDispatcher implements Listener {

    /** The BungeeCord proxy server used to find online players. */
    private final ProxyServer proxy;

    /** Query timeout in seconds. */
    private final long timeoutSeconds;

    /** Optional Redis manager for cross-proxy query routing (null when disabled). */
    private volatile RedisManager redisManager;

    /** Map of pending request futures keyed by their request ID. */
    private final ConcurrentHashMap<String, CompletableFuture<Map<String, String>>> pending;

    /**
     * Constructs a dispatcher.
     *
     * @param proxy          the BungeeCord proxy server
     * @param timeoutSeconds query timeout in seconds
     */
    public BungeeQueryDispatcher(final ProxyServer proxy, final long timeoutSeconds) {
        this.proxy = proxy;
        this.timeoutSeconds = timeoutSeconds;
        this.pending = new ConcurrentHashMap<>();
    }

    /**
     * Attaches an optional Redis manager for cross-proxy routing.
     * May be called at any time after construction.
     *
     * @param manager the Redis manager, or {@code null} to disable Redis routing
     */
    public void setRedisManager(final RedisManager manager) {
        this.redisManager = manager;
    }

    /**
     * Sends a query to a backend server and returns a future for the response.
     *
     * <p>The message is routed through the given preferred player if they are online and
     * connected to a backend. If {@code preferredPlayer} is not available, any connected
     * player is used as a fallback. When no local player is available and Redis is enabled,
     * the query is published to Redis for a remote proxy to fulfil. The future completes
     * exceptionally with {@link IllegalStateException} when no local player exists and
     * Redis is disabled, or with {@link java.util.concurrent.TimeoutException} after the
     * timeout.</p>
     *
     * @param preferredPlayer UUID of the player to prefer for routing
     * @param op              the operation name
     * @param args            additional key-value arguments for the operation
     * @return a future that resolves to the parsed response map
     */
    public CompletableFuture<Map<String, String>> query(final UUID preferredPlayer,
            final String op, final Map<String, String> args) {
        ProxiedPlayer route = proxy.getPlayer(preferredPlayer);
        if (route == null || route.getServer() == null) {
            route = proxy.getPlayers().stream()
                .filter(p -> p.getServer() != null)
                .findFirst()
                .orElse(null);
        }
        if (route == null) {
            final RedisManager rm = redisManager;
            if (rm != null) {
                return rm.query(op, args)
                    .orTimeout(timeoutSeconds, TimeUnit.SECONDS);
            }
            return CompletableFuture.failedFuture(
                new IllegalStateException(
                    "No online player available to route the bridge query."
                    + " Enable Redis for multi-proxy support."));
        }
        final String reqId = UUID.randomUUID().toString();
        final CompletableFuture<Map<String, String>> future =
            new CompletableFuture<Map<String, String>>().orTimeout(timeoutSeconds, TimeUnit.SECONDS);
        pending.put(reqId, future);
        future.whenComplete((r, t) -> pending.remove(reqId));
        final byte[] payload = BridgeProtocol.buildRequest(reqId, op, args);
        route.getServer().sendData(BridgeProtocol.CHANNEL_NAME, payload);
        return future;
    }

    /**
     * Routes a query with any available local player, without a preference.
     * Used when the query is not tied to a specific player.
     *
     * @param op   the operation name
     * @param args operation arguments
     * @return a future that resolves to the parsed response map
     */
    public CompletableFuture<Map<String, String>> queryAny(final String op,
            final Map<String, String> args) {
        return query(UUID.randomUUID(), op, args);
    }

    /**
     * Handles inbound plugin messages from backend servers.
     * Matches each message to a pending future and resolves it.
     *
     * @param event the plugin message event
     */
    @EventHandler
    public void onPluginMessage(final PluginMessageEvent event) {
        if (!(event.getSender() instanceof Server)) {
            return;
        }
        if (!event.getTag().equals(BridgeProtocol.CHANNEL_NAME)) {
            return;
        }
        event.setCancelled(true);
        final Map<String, String> response = BridgeProtocol.parseResponse(event.getData());
        final String reqId = BridgeProtocol.getString(response, "reqId");
        if (reqId == null) {
            return;
        }
        final CompletableFuture<Map<String, String>> future = pending.remove(reqId);
        if (future == null) {
            return;
        }
        future.complete(response);
    }
}
