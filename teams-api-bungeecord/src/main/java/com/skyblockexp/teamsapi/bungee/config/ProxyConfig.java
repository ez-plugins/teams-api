package com.skyblockexp.teamsapi.bungee.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * Loads and exposes configuration values for the TeamsAPI BungeeCord plugin.
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
     * @param dataFolder the plugin data folder (from {@code Plugin.getDataFolder()})
     * @param pluginJarStream stream to the default config.yml bundled in the JAR
     * @return the loaded configuration
     */
    public static ProxyConfig load(final File dataFolder,
            final InputStream pluginJarStream) {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            final File configFile = new File(dataFolder, "config.yml");
            if (!configFile.exists() && pluginJarStream != null) {
                Files.copy(pluginJarStream, configFile.toPath());
            }
            if (configFile.exists()) {
                final Configuration cfg = ConfigurationProvider
                    .getProvider(YamlConfiguration.class)
                    .load(configFile);
                return parse(cfg);
            }
        }
        catch (IOException e) {
            // fall through to defaults
        }
        return defaults();
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
     * Parses a BungeeCord {@link Configuration} into a {@link ProxyConfig}.
     *
     * @param cfg the configuration loaded by BungeeCord's YAML provider
     * @return the parsed configuration
     */
    static ProxyConfig parse(final Configuration cfg) {
        return new ProxyConfig(
            cfg.getBoolean("redis.enabled", false),
            cfg.getString("redis.host", "127.0.0.1"),
            cfg.getInt("redis.port", 6379),
            cfg.getString("redis.password", ""),
            cfg.getInt("redis.database", 0),
            cfg.getString("redis.prefix", "teamsapi:"),
            cfg.getInt("redis.pool.max-total", 8),
            cfg.getInt("redis.pool.max-idle", 4),
            cfg.getInt("redis.pool.min-idle", 1),
            cfg.getInt("redis.timeout-ms", 3000),
            cfg.getInt("query.timeout-seconds", DEFAULT_TIMEOUT));
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
}
