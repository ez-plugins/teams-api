package com.skyblockexp.teamsapi.velocity.bridge;

import com.skyblockexp.teamsapi.velocity.redis.RedisManager;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
 * distributed across multiple Velocity instances.</p>
 *
 * <p>Futures time out after the configured number of seconds if no response is received.
 * Pending entries are cleaned up automatically on completion or timeout.</p>
 */
public final class TeamQueryDispatcher {

    /** The Velocity proxy server used to find online players. */
    private final ProxyServer server;

    /** The channel identifier for inbound and outbound bridge messages. */
    private final MinecraftChannelIdentifier channel;

    /** Query timeout in seconds. */
    private final long timeoutSeconds;

    /** Optional Redis manager for cross-proxy query routing (null when disabled). */
    private volatile RedisManager redisManager;

    /** Map of pending request futures keyed by their request ID. */
    private final ConcurrentHashMap<String, CompletableFuture<Map<String, String>>> pending;

    /**
     * Constructs a dispatcher.
     *
     * @param server         the Velocity proxy server
     * @param channel        the plugin messaging channel identifier
     * @param timeoutSeconds query timeout in seconds
     */
    public TeamQueryDispatcher(final ProxyServer server,
            final MinecraftChannelIdentifier channel, final long timeoutSeconds) {
        this.server = server;
        this.channel = channel;
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
     * Routes a query through a local player if available; falls back to Redis when not.
     *
     * <p>The message is routed through the given preferred player if they are online.
     * If {@code preferredPlayer} is not online, any connected player is used as a fallback.
     * When no local player is available and Redis is enabled, the query is published to
     * Redis for a remote proxy to fulfil. The future completes exceptionally with
     * {@link IllegalStateException} when no local player exists and Redis is disabled,
     * or with {@link java.util.concurrent.TimeoutException} after the timeout.</p>
     *
     * @param preferredPlayer UUID of the player to prefer for routing (e.g. the query subject)
     * @param op              the operation name
     * @param args            additional key-value arguments for the operation
     * @return a future that resolves to the parsed response map
     */
    public CompletableFuture<Map<String, String>> query(final UUID preferredPlayer,
            final String op, final Map<String, String> args) {
        final Optional<com.velocitypowered.api.proxy.Player> route =
            server.getPlayer(preferredPlayer)
                .filter(p -> p.getCurrentServer().isPresent())
                .or(() -> server.getAllPlayers().stream()
                    .filter(p -> p.getCurrentServer().isPresent())
                    .findFirst());

        if (route.isEmpty()) {
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
        final CompletableFuture<Map<String, String>> future = new CompletableFuture<
            Map<String, String>>().orTimeout(timeoutSeconds, TimeUnit.SECONDS);

        pending.put(reqId, future);
        future.whenComplete((r, t) -> pending.remove(reqId));

        final byte[] payload = BridgeProtocol.buildRequest(reqId, op, args);
        route.get().getCurrentServer().ifPresent(conn -> conn.sendPluginMessage(channel, payload));

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
    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }
        if (!event.getIdentifier().equals(channel)) {
            return;
        }
        // Mark handled so Velocity does not forward this internal message to clients
        event.setResult(PluginMessageEvent.ForwardResult.handled());

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
