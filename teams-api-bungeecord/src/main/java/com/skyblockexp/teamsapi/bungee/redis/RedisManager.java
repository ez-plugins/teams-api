package com.skyblockexp.teamsapi.bungee.redis;

import com.skyblockexp.teamsapi.bungee.bridge.BridgeProtocol;
import com.skyblockexp.teamsapi.bungee.config.ProxyConfig;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

/**
 * Manages a Redis connection pool and pub/sub subscriber for multi-proxy coordination.
 *
 * <h2>Channel layout</h2>
 * <pre>
 *   {prefix}request            -- all proxies subscribe; any can fulfil a request
 *   {prefix}response:{proxyId} -- this proxy subscribes; others publish responses here
 * </pre>
 *
 * <h2>Message format (JSON-like flat map, same encoding as BridgeProtocol)</h2>
 * <pre>
 *   Request  : {"reqId":"...","replyTo":"{proxyId}","op":"...","arg:key":"val",...}
 *   Response : {"reqId":"...","data:key":"val",...}
 * </pre>
 *
 * <p>When a remote request arrives on {@code {prefix}request}, this manager tries to
 * fulfil it through its own local players. If successful, it publishes the response
 * back to {@code {prefix}response:{replyTo}}.</p>
 */
public final class RedisManager {

    /** Prefix used for encoding args inside a Redis message. */
    private static final String ARG_PREFIX = "arg:";

    /** Prefix used for encoding data inside a Redis response message. */
    private static final String DATA_PREFIX = "data:";

    /** This proxy's unique identifier (stable per JVM startup). */
    private final String proxyId;

    /** Channel prefix from config. */
    private final String prefix;

    /** Logger. */
    private final Logger logger;

    /** Connection pool for publish operations. */
    private final JedisPool pool;

    /** Subscriber thread executor. */
    private final ExecutorService subscriberExecutor;

    /** Pending cross-proxy futures, keyed by reqId. */
    private final ConcurrentHashMap<String, CompletableFuture<Map<String, String>>> pending;

    /** The pub/sub handler (held so we can unsubscribe cleanly). */
    private volatile BridgePubSub pubSub;

    /** Callback: given (op, args) returns a future from the local query dispatcher. */
    private final BiFunction<String, Map<String, String>, CompletableFuture<Map<String, String>>>
        localDispatch;

    /**
     * Creates and connects a Redis manager.
     *
     * @param config        plugin config
     * @param logger        JUL logger
     * @param localDispatch function that routes a query through a local player
     */
    public RedisManager(final ProxyConfig config, final Logger logger,
            final BiFunction<String, Map<String, String>,
                CompletableFuture<Map<String, String>>> localDispatch) {
        this.proxyId = UUID.randomUUID().toString();
        this.prefix = config.getRedisPrefix();
        this.logger = logger;
        this.localDispatch = localDispatch;
        this.pending = new ConcurrentHashMap<>();
        this.subscriberExecutor = Executors.newSingleThreadExecutor(r -> {
            final Thread t = new Thread(r, "teamsapi-redis-sub");
            t.setDaemon(true);
            return t;
        });

        final JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(config.getPoolMaxTotal());
        poolCfg.setMaxIdle(config.getPoolMaxIdle());
        poolCfg.setMinIdle(config.getPoolMinIdle());

        final String password = config.getRedisPassword();
        if (password == null || password.isEmpty()) {
            this.pool = new JedisPool(poolCfg, config.getRedisHost(),
                config.getRedisPort(), config.getRedisTimeoutMs());
        }
        else {
            this.pool = new JedisPool(poolCfg, config.getRedisHost(),
                config.getRedisPort(), config.getRedisTimeoutMs(), password,
                config.getRedisDatabase());
        }
    }

    /**
     * Starts the pub/sub subscriber background thread.
     * Must be called after construction before any queries are dispatched.
     */
    public void start() {
        pubSub = new BridgePubSub();
        subscriberExecutor.submit(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(pubSub,
                    prefix + "request",
                    prefix + "response:" + proxyId);
            }
            catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    logger.severe("TeamsAPI Redis subscriber thread stopped unexpectedly: "
                        + e.getMessage());
                }
            }
        });
        logger.info("TeamsAPI Redis multi-proxy bridge active. Proxy ID: " + proxyId);
    }

    /**
     * Publishes a query to Redis for any proxy in the network to fulfil.
     *
     * @param op   the operation name
     * @param args operation arguments
     * @return a future resolved when a remote proxy responds
     */
    public CompletableFuture<Map<String, String>> query(final String op,
            final Map<String, String> args) {
        final String reqId = UUID.randomUUID().toString();
        final CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
        pending.put(reqId, future);
        future.whenComplete((r, t) -> pending.remove(reqId));

        final Map<String, String> msg = new LinkedHashMap<>();
        msg.put("reqId", reqId);
        msg.put("replyTo", proxyId);
        msg.put("op", op);
        for (final Map.Entry<String, String> entry : args.entrySet()) {
            msg.put(ARG_PREFIX + entry.getKey(), entry.getValue());
        }

        try (Jedis jedis = pool.getResource()) {
            jedis.publish(prefix + "request", encode(msg));
        }
        catch (Exception e) {
            pending.remove(reqId);
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * Shuts down the Redis connection pool and subscriber thread.
     */
    public void shutdown() {
        final BridgePubSub sub = pubSub;
        if (sub != null) {
            try {
                sub.unsubscribe();
            }
            catch (Exception e) {
                // ignore during shutdown
            }
        }
        subscriberExecutor.shutdownNow();
        pool.close();
    }

    // ── Encoding helpers ──────────────────────────────────────────────────────

    private static String encode(final Map<String, String> map) {
        final StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (final Map.Entry<String, String> e : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            sb.append('"').append(escape(e.getKey()))
              .append("\":\"").append(escape(e.getValue())).append('"');
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escape(final String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Map<String, String> decode(final String json) {
        return BridgeProtocol.parseResponse(json.getBytes(StandardCharsets.UTF_8));
    }

    // ── Inner pub/sub handler ─────────────────────────────────────────────────

    /**
     * Handles incoming Redis messages on the subscribed channels.
     */
    private final class BridgePubSub extends JedisPubSub {

        /**
         * Constructs the pub/sub handler.
         */
        BridgePubSub() {
        }

        /**
         * Called by Jedis when a message arrives on a subscribed channel.
         *
         * @param channel the Redis channel name
         * @param message the message payload
         */
        @Override
        public void onMessage(final String channel, final String message) {
            if (channel.equals(prefix + "request")) {
                handleIncomingRequest(message);
            }
            else if (channel.equals(prefix + "response:" + proxyId)) {
                handleIncomingResponse(message);
            }
        }
    }

    /**
     * Handles a query request that arrived from another proxy.
     *
     * @param message the raw Redis message
     */
    private void handleIncomingRequest(final String message) {
        final Map<String, String> msg = decode(message);
        final String reqId = msg.get("reqId");
        final String replyTo = msg.get("replyTo");
        final String op = msg.get("op");

        if (proxyId.equals(replyTo)) {
            return;
        }
        if (reqId == null || replyTo == null || op == null) {
            return;
        }

        final Map<String, String> args = new LinkedHashMap<>();
        for (final Map.Entry<String, String> entry : msg.entrySet()) {
            if (entry.getKey().startsWith(ARG_PREFIX)) {
                args.put(entry.getKey().substring(ARG_PREFIX.length()), entry.getValue());
            }
        }

        localDispatch.apply(op, args)
            .thenAccept(data -> {
                final Map<String, String> resp = new LinkedHashMap<>();
                resp.put("reqId", reqId);
                for (final Map.Entry<String, String> entry : data.entrySet()) {
                    resp.put(DATA_PREFIX + entry.getKey(), entry.getValue());
                }
                try (Jedis jedis = pool.getResource()) {
                    jedis.publish(prefix + "response:" + replyTo, encode(resp));
                }
                catch (Exception e) {
                    logger.warning("Failed to publish Redis response for req " + reqId
                        + ": " + e.getMessage());
                }
            })
            .exceptionally(t -> null);
    }

    /**
     * Handles a response from a remote proxy for one of our pending futures.
     *
     * @param message the raw Redis message
     */
    private void handleIncomingResponse(final String message) {
        final Map<String, String> msg = decode(message);
        final String reqId = msg.get("reqId");
        if (reqId == null) {
            return;
        }
        final CompletableFuture<Map<String, String>> future = pending.remove(reqId);
        if (future == null) {
            return;
        }
        final Map<String, String> data = new LinkedHashMap<>();
        for (final Map.Entry<String, String> entry : msg.entrySet()) {
            if (entry.getKey().startsWith(DATA_PREFIX)) {
                data.put(entry.getKey().substring(DATA_PREFIX.length()), entry.getValue());
            }
        }
        future.complete(data);
    }
}
