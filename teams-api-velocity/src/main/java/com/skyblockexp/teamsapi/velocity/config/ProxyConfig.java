package com.skyblockexp.teamsapi.velocity.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Loads and exposes configuration values for the TeamsAPI Velocity plugin.
 *
 * <p>On first startup the default {@code config.yml} bundled in the JAR is copied
 * to the plugin data directory. Subsequent starts load from that file so server
 * owners can customise values without modifying the JAR.</p>
 *
 * <p>All fields have safe defaults so the plugin functions correctly even if the
 * config file is absent or partially populated.</p>
 */
public final class ProxyConfig {

    /** Default query timeout in seconds. */
    private static final int DEFAULT_TIMEOUT = 5;

    /** Whether Redis multi-proxy coordination is enabled. */
    private final boolean redisEnabled;

    /** Redis server hostname. */
    private final String redisHost;

    /** Redis server port. */
    private final int redisPort;

    /** Redis AUTH password (may be empty). */
    private final String redisPassword;

    /** Redis logical database index. */
    private final int redisDatabase;

    /** Key/channel prefix for all TeamsAPI Redis traffic. */
    private final String redisPrefix;

    /** Redis connection pool maximum total connections. */
    private final int poolMaxTotal;

    /** Redis connection pool maximum idle connections. */
    private final int poolMaxIdle;

    /** Redis connection pool minimum idle connections. */
    private final int poolMinIdle;

    /** Redis socket timeout in milliseconds. */
    private final int redisTimeoutMs;

    /** Bridge query timeout in seconds. */
    private final int queryTimeoutSeconds;

    /**
     * Loads configuration from the plugin data directory, copying defaults if absent.
     *
     * @param dataDir the plugin data directory (injected via {@code @DataDirectory})
     * @return the loaded configuration
     */
    public static ProxyConfig load(final Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            final Path configFile = dataDir.resolve("config.yml");
            if (!Files.exists(configFile)) {
                try (InputStream in =
                        ProxyConfig.class.getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    }
                }
            }
            try (InputStream in = Files.newInputStream(configFile)) {
                return parse(new Yaml().load(in));
            }
        }
        catch (IOException e) {
            return defaults();
        }
    }

    /**
     * Returns a config instance populated entirely with default values.
     * Used as a fallback when the config file cannot be read.
     *
     * @return default configuration
     */
    public static ProxyConfig defaults() {
        return new ProxyConfig(false, "127.0.0.1", 6379, "", 0, "teamsapi:",
            8, 4, 1, 3000, DEFAULT_TIMEOUT);
    }

    /**
     * Parses a raw YAML map produced by SnakeYAML into a {@link ProxyConfig}.
     *
     * @param root the top-level map from the YAML file
     * @return the parsed configuration
     */
    @SuppressWarnings("unchecked")
    static ProxyConfig parse(final Map<String, Object> root) {
        if (root == null) {
            return defaults();
        }
        final Map<String, Object> redis =
            (Map<String, Object>) root.getOrDefault("redis", Map.of());
        final Map<String, Object> pool =
            (Map<String, Object>) redis.getOrDefault("pool", Map.of());
        final Map<String, Object> query =
            (Map<String, Object>) root.getOrDefault("query", Map.of());

        return new ProxyConfig(
            getBool(redis, "enabled", false),
            getString(redis, "host", "127.0.0.1"),
            getInt(redis, "port", 6379),
            getString(redis, "password", ""),
            getInt(redis, "database", 0),
            getString(redis, "prefix", "teamsapi:"),
            getInt(pool, "max-total", 8),
            getInt(pool, "max-idle", 4),
            getInt(pool, "min-idle", 1),
            getInt(redis, "timeout-ms", 3000),
            getInt(query, "timeout-seconds", DEFAULT_TIMEOUT));
    }

    /**
     * Constructs a configuration with all values explicitly specified.
     *
     * @param redisEnabled         whether Redis coordination is active
     * @param redisHost            Redis server hostname
     * @param redisPort            Redis server port
     * @param redisPassword        Redis AUTH password (empty string for none)
     * @param redisDatabase        Redis logical database index
     * @param redisPrefix          prefix for all TeamsAPI Redis keys/channels
     * @param poolMaxTotal         connection pool max total
     * @param poolMaxIdle          connection pool max idle
     * @param poolMinIdle          connection pool min idle
     * @param redisTimeoutMs       Redis socket timeout in milliseconds
     * @param queryTimeoutSeconds  bridge query timeout in seconds
     */
    public ProxyConfig(final boolean redisEnabled, final String redisHost,
            final int redisPort, final String redisPassword, final int redisDatabase,
            final String redisPrefix, final int poolMaxTotal, final int poolMaxIdle,
            final int poolMinIdle, final int redisTimeoutMs, final int queryTimeoutSeconds) {
        this.redisEnabled = redisEnabled;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
        this.redisDatabase = redisDatabase;
        this.redisPrefix = redisPrefix;
        this.poolMaxTotal = poolMaxTotal;
        this.poolMaxIdle = poolMaxIdle;
        this.poolMinIdle = poolMinIdle;
        this.redisTimeoutMs = redisTimeoutMs;
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    /**
     * Returns whether Redis multi-proxy coordination is enabled.
     *
     * @return true when Redis is enabled
     */
    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    /**
     * Returns the Redis server hostname.
     *
     * @return hostname or IP address
     */
    public String getRedisHost() {
        return redisHost;
    }

    /**
     * Returns the Redis server port.
     *
     * @return port number
     */
    public int getRedisPort() {
        return redisPort;
    }

    /**
     * Returns the Redis AUTH password, or an empty string if none is configured.
     *
     * @return the Redis password
     */
    public String getRedisPassword() {
        return redisPassword;
    }

    /**
     * Returns the Redis logical database index.
     *
     * @return database index (0-15)
     */
    public int getRedisDatabase() {
        return redisDatabase;
    }

    /**
     * Returns the prefix applied to all TeamsAPI Redis keys and channels.
     *
     * @return the key prefix
     */
    public String getRedisPrefix() {
        return redisPrefix;
    }

    /**
     * Returns the maximum total connections in the Redis connection pool.
     *
     * @return pool max-total
     */
    public int getPoolMaxTotal() {
        return poolMaxTotal;
    }

    /**
     * Returns the maximum idle connections in the Redis connection pool.
     *
     * @return pool max-idle
     */
    public int getPoolMaxIdle() {
        return poolMaxIdle;
    }

    /**
     * Returns the minimum idle connections in the Redis connection pool.
     *
     * @return pool min-idle
     */
    public int getPoolMinIdle() {
        return poolMinIdle;
    }

    /**
     * Returns the Redis socket timeout in milliseconds.
     *
     * @return timeout in milliseconds
     */
    public int getRedisTimeoutMs() {
        return redisTimeoutMs;
    }

    /**
     * Returns the bridge query timeout in seconds.
     *
     * @return timeout in seconds
     */
    public int getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static boolean getBool(final Map<String, Object> map,
            final String key, final boolean def) {
        final Object val = map.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return def;
    }

    private static String getString(final Map<String, Object> map,
            final String key, final String def) {
        final Object val = map.get(key);
        if (val instanceof String s) {
            return s;
        }
        return def;
    }

    private static int getInt(final Map<String, Object> map,
            final String key, final int def) {
        final Object val = map.get(key);
        if (val instanceof Number n) {
            return n.intValue();
        }
        return def;
    }
}
