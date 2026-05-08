---
title: BungeeCord Guide
nav_order: 8
description: "Setting up and using the TeamsAPI BungeeCord bridge plugin"
---

# BungeeCord Guide
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

The `teams-api-bungeecord` plugin bridges TeamsAPI from Bukkit backends to your
BungeeCord (or Waterfall) proxy. Proxy-side plugins can query team data
asynchronously without connecting directly to each backend server.

## How it works

```text
+------------------------------+
|  Your BungeeCord plugin      |  calls BungeeTeamsAPI.getService()
+--------------+---------------+
               |  CompletableFuture<T>
               v
+------------------------------+
|  teams-api-bungeecord        |  installed as a BungeeCord plugin
|  BungeeQueryDispatcher       |  routes messages over teamsapi:bridge
+--------------+---------------+
               |  plugin messaging (teamsapi:bridge channel)
               v
+------------------------------+
|  teams-api-plugin (Bukkit)   |  installed on each backend server
|  PluginBootstrap             |  handles bridge requests, returns JSON
+------------------------------+
```

Each query is sent to a backend server through the first available online player.
Responses arrive asynchronously and resolve a `CompletableFuture`. Requests time
out after **5 seconds** if no response arrives.

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | **17** or newer |
| BungeeCord / Waterfall | any recent release |
| Backend | Paper, Spigot, Purpur, or Folia **1.16+** |
| Backend plugin | `teams-api-plugin` AND a `TeamsService` provider |

## Installation

### Step 1: Download both JARs

From the [Releases page](https://github.com/ez-plugins/teams-api/releases) download:

- `teams-api-plugin-VERSION.jar` -- install in every **backend** server's `plugins/`
  directory
- `teams-api-bungeecord-VERSION.jar` -- install in the **BungeeCord proxy's** `plugins/`
  directory

### Step 2: Install the backend plugin

Place `teams-api-plugin-VERSION.jar` in `plugins/` on **each** backend server that
should answer team queries. Each backend must also have a `TeamsService` provider
installed (your team plugin).

### Step 3: Install the BungeeCord plugin

Place `teams-api-bungeecord-VERSION.jar` in your BungeeCord proxy's `plugins/` directory.

### Step 4: Restart everything

Restart your backend servers, then restart (or reload) your BungeeCord proxy.

When the BungeeCord plugin initialises it will log:

```
[TeamsAPI] TeamsAPI BungeeCord bridge ready. Channel: teamsapi:bridge
```

## For consumers (BungeeCord plugins)

### 1. Add the dependency

**Maven** (via [JitPack](https://jitpack.io/#ez-plugins/teams-api)):

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ez-plugins</groupId>
    <artifactId>teams-api-bungeecord</artifactId>
    <version>1.3.0</version>
    <scope>provided</scope>
</dependency>
```

**Gradle**:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api-bungeecord:1.3.0'
}
```

### 2. Declare the dependency in `bungee.yml`

```yaml
depends:
  - TeamsAPI
```

Or use `softDepends` if team support is optional:

```yaml
softDepends:
  - TeamsAPI
```

### 3. Check availability

```java
if (!BungeeTeamsAPI.isAvailable()) {
    getLogger().warning("TeamsAPI bridge not active -- team features disabled.");
    return;
}
BungeeTeamsService service = BungeeTeamsAPI.getService();
```

### 4. Query team data

All methods return `CompletableFuture<T>`. Handle completion on BungeeCord's
scheduler or a dedicated executor:

```java
// Check whether a player is in a team
service.hasTeam(player.getUniqueId())
    .thenAccept(inTeam -> {
        if (inTeam) {
            player.sendMessage(new TextComponent("You are in a team!"));
        }
    });

// Fetch the player's team
service.getPlayerTeam(player.getUniqueId())
    .thenAccept(optional -> optional.ifPresentOrElse(
        team -> player.sendMessage(new TextComponent("Team: " + team.getName())),
        () -> player.sendMessage(new TextComponent("You are not in a team."))
    ));

// Get the team count
service.getTeamCount()
    .thenAccept(count -> getLogger().info("Total teams: " + count));
```

### 5. Handle errors

```java
service.getPlayerTeam(player.getUniqueId())
    .thenAccept(opt -> opt.ifPresent(team -> {
        // handle team
    }))
    .exceptionally(ex -> {
        // TimeoutException: backend did not respond within 5 seconds
        // IllegalStateException: no online player to route through
        getLogger().warning("Team query failed: " + ex.getMessage());
        return null;
    });
```

## API reference

### `BungeeTeamsAPI` (static facade)

| Method | Description |
|--------|-------------|
| `isAvailable()` | Returns `true` when the bridge is active and a service is registered. |
| `getService()` | Returns the active `BungeeTeamsService`, or `null` if unavailable. |

### `BungeeTeamsService` (interface)

All methods return `CompletableFuture<T>` and may complete exceptionally with
`TimeoutException` (no response in 5 s) or `IllegalStateException` (no route).

| Method | Returns | Description |
|--------|---------|-------------|
| `hasTeam(playerUUID)` | `CompletableFuture<Boolean>` | Whether the player belongs to any team. |
| `getPlayerTeam(playerUUID)` | `CompletableFuture<Optional<BungeeTeam>>` | The team the player belongs to, if any. |
| `getTeam(teamId)` | `CompletableFuture<Optional<BungeeTeam>>` | Finds a team by UUID. |
| `getTeamByName(name)` | `CompletableFuture<Optional<BungeeTeam>>` | Finds a team by name. |
| `getTeamCount()` | `CompletableFuture<Integer>` | Total number of teams on the backend. |
| `teamExists(name)` | `CompletableFuture<Boolean>` | Whether a team with that name exists. |
| `isMember(teamId, playerUUID)` | `CompletableFuture<Boolean>` | Whether the player is a member of the team. |
| `getMemberRole(teamId, playerUUID)` | `CompletableFuture<Optional<BungeeTeamRole>>` | The player's role within the team, if a member. |
| `getMemberInfo(teamId, playerUUID)` | `CompletableFuture<Optional<BungeeTeamMember>>` | The player's full membership record, if a member. |
| `getAllTeams()` | `CompletableFuture<Collection<BungeeTeam>>` | All teams registered on the backend. |

### `BungeeTeam` (interface)

| Method | Returns | Description |
|--------|---------|-------------|
| `getId()` | `UUID` | Stable unique identifier for this team. |
| `getName()` | `String` | Internal team name. |
| `getDisplayName()` | `String` | Display name (may include formatting). |
| `getOwnerUUID()` | `UUID` | UUID of the team owner. |
| `getSize()` | `int` | Current member count. |
| `getMaxSize()` | `int` | Maximum member cap; `-1` means unlimited. |

### `BungeeTeamMember` (interface)

| Method | Returns | Description |
|--------|---------|-------------|
| `getPlayerUUID()` | `UUID` | The player's UUID. |
| `getRole()` | `BungeeTeamRole` | The role the player holds. |

### `BungeeTeamRole` (enum)

| Constant | Priority | Description |
|----------|----------|-------------|
| `OWNER` | 100 | Team owner. Full authority. |
| `ADMIN` | 50 | Team administrator. Can manage regular members. |
| `MEMBER` | 10 | Regular team member. |

## Multi-proxy setup (Redis)

By default the bridge routes every query through a player connected to **this** proxy.
When your network spans multiple BungeeCord instances, a player's backend may be
reachable only from a different proxy. Enabling Redis lets all proxies share queries
over a Pub/Sub channel so any proxy can fulfil a request for any player.

### How it works

```text
+---------------------+    teamsapi:request     +---------------------+
|  Proxy A            | ──────────────────────► |  Redis              |
|  (receives query)   |                          |  (Pub/Sub broker)   |
+---------------------+ ◄────────────────────── +----------+----------+
         ^               teamsapi:response:A               |
         |                                        teamsapi:request
         |                                                  |
         |                                                  v
         |                                       +---------------------+
         |                                       |  Proxy B            |
         +-- future resolved <─────────────────  |  (fulfils query via |
                                                  |   local player)     |
                                                  +---------------------+
```

1. Proxy A cannot find the target player locally; it publishes the query to
   `{prefix}request` on Redis.
2. All other proxies are subscribed to that channel and attempt to fulfil the query
   through their own local players.
3. The first proxy that successfully queries its backend publishes the result to
   `{prefix}response:{proxyAId}`.
4. Proxy A resolves the pending `CompletableFuture` with that result.

### Redis configuration

After the first startup a `config.yml` is created in the plugin data folder:

```yaml
redis:
  enabled: false          # set to true to enable multi-proxy mode
  host: "127.0.0.1"
  port: 6379
  password: ""            # leave empty for unauthenticated Redis
  database: 0             # Redis logical database index (0-15)
  prefix: "teamsapi:"     # prefix for all keys and channels

  pool:
    max-total: 8
    max-idle: 4
    min-idle: 1

  timeout-ms: 3000        # Redis socket timeout in milliseconds

query:
  timeout-seconds: 5      # bridge query timeout (local + Redis)
```

Set `redis.enabled: true` and point `host` / `port` at your Redis instance,
then restart the proxy. All BungeeCord proxies in the network must share the same
Redis instance and use identical `prefix` values.

### Requirements

- Redis 6+ (Redis 7 recommended)
- All proxies must use the same Redis instance and `prefix`
- All proxies must have `teams-api-bungeecord` installed with Redis enabled

## Limitations

- **Read-only**: `BungeeTeamsService` supports queries only. Mutations must go
  through your backend plugin.
- **Route dependency (single proxy)**: when Redis is disabled, every query is routed
  through an online player on this proxy. If no player is connected to a backend,
  queries will fail with `IllegalStateException`.
- **Single backend per query**: the query is sent to whichever backend the routing
  player is on. Consistent multi-backend routing requires custom routing logic.
- **No invite/warp support**: the BungeeCord bridge covers `TeamsService` only.

## See also

- [Velocity Guide](velocity): Velocity proxy bridge (same bridge protocol, different proxy)
- [Server Guide](server-guide): installing `teams-api-plugin` on a backend server
- [Developer Guide](developer-guide): full consumer guide for Bukkit plugins
- [API Reference](api): complete interface tables for all Bukkit-side types
