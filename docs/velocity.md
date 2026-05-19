---
title: Velocity Guide
nav_order: 4
parent: Consumer Guide
description: "Setting up and using the TeamsAPI Velocity bridge plugin"
---

# Velocity Guide
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

The `teams-api-velocity` plugin bridges TeamsAPI from Bukkit backends to your
Velocity proxy. Proxy-side plugins can query team data asynchronously without
connecting directly to each backend server.

## How it works

```text
┌──────────────────────────────┐
│  Your Velocity plugin        │  calls VelocityTeamsAPI.getService()
└──────────────┬───────────────┘
               │  CompletableFuture<T>
               ▼
┌──────────────────────────────┐
│  teams-api-velocity          │  installed as a Velocity plugin
│  TeamQueryDispatcher         │  routes messages over teamsapi:bridge
└──────────────┬───────────────┘
               │  plugin messaging (teamsapi:bridge channel)
               ▼
┌──────────────────────────────┐
│  teams-api-plugin (Bukkit)   │  installed on each backend server
│  PluginBootstrap             │  handles bridge requests, returns JSON
└──────────────────────────────┘
```text

Each query is sent to a backend server through the first available online player.
Responses arrive asynchronously and resolve a `CompletableFuture`. Requests time
out after **5 seconds** if no response arrives.

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | **17** or newer |
| Velocity | **3.x** |
| Backend | Paper, Spigot, Purpur, or Folia **1.16+** |
| Backend plugin | `teams-api-plugin` AND a `TeamsService` provider |

## Installation

### Step 1: Download both JARs

From the [Releases page](https://github.com/ez-plugins/teams-api/releases) download:

- `teams-api-plugin-VERSION.jar` — install in every **backend** server's `plugins/`
  directory
- `teams-api-velocity-VERSION.jar` — install in the **Velocity proxy's** `plugins/`
  directory

### Step 2: Install the backend plugin

Place `teams-api-plugin-VERSION.jar` in `plugins/` on **each** backend server that
should answer team queries. Each backend must also have a `TeamsService` provider
installed (your team plugin).

### Step 3: Install the Velocity plugin

Place `teams-api-velocity-VERSION.jar` in your Velocity proxy's `plugins/` directory.

### Step 4: Restart everything

Restart your backend servers, then restart (or reload) your Velocity proxy.

When the Velocity plugin initialises it will log:

```text
[TeamsAPI] Velocity bridge ready. Channel: teamsapi:bridge
```text

## For consumers (Velocity plugins)

### 1. Add the dependency

**Maven** (via JitPack):

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ez-plugins</groupId>
    <artifactId>teams-api-velocity</artifactId>
    <version>1.3.0</version>
    <scope>provided</scope>
</dependency>
```text

**Gradle**:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api-velocity:1.3.0'
}
```text

### 2. Declare the dependency in `velocity-plugin.json`

```json
{
  "dependencies": [
    { "id": "teams-api-velocity", "optional": true }
  ]
}
```text

Mark it `optional` if team support is not required for your plugin to function.

### 3. Check availability

```java
if (!VelocityTeamsAPI.isAvailable()) {
    logger.warn("TeamsAPI bridge not active — team features disabled.");
    return;
}
VelocityTeamsService service = VelocityTeamsAPI.getService();
```text

### 4. Query team data

All methods return `CompletableFuture<T>`. Handle completion on Velocity's event
thread or a dedicated executor:

```java
// Check whether a player is in a team
service.hasTeam(player.getUniqueId())
    .thenAccept(inTeam -> {
        if (inTeam) {
            player.sendMessage(Component.text("You are in a team!"));
        }
    });

// Fetch the player's team
service.getPlayerTeam(player.getUniqueId())
    .thenAccept(optional -> optional.ifPresentOrElse(
        team -> player.sendMessage(Component.text("Team: " + team.getName())),
        () -> player.sendMessage(Component.text("You are not in a team."))
    ));

// Get the team count
service.getTeamCount()
    .thenAccept(count -> logger.info("Total teams: " + count));
```text

### 5. Handle errors

```java
service.getPlayerTeam(player.getUniqueId())
    .thenAccept(opt -> opt.ifPresent(team -> {
        // handle team
    }))
    .exceptionally(ex -> {
        // TimeoutException: backend did not respond within 5 seconds
        // IllegalStateException: no online player to route through
        logger.warn("Team query failed: " + ex.getMessage());
        return null;
    });
```text

## API reference

### `VelocityTeamsAPI` (static facade)

| Method | Description |
|--------|-------------|
| `isAvailable()` | Returns `true` when the bridge is active and a service is registered. |
| `getService()` | Returns the active `VelocityTeamsService`, or `null` if unavailable. |

### `VelocityTeamsService` (interface)

All methods return `CompletableFuture<T>` and may complete exceptionally with
`TimeoutException` (no response in 5 s) or `IllegalStateException` (no route).

| Method | Returns | Description |
|--------|---------|-------------|
| `hasTeam(playerUUID)` | `CompletableFuture<Boolean>` | Whether the player belongs to any team. |
| `getPlayerTeam(playerUUID)` | `CompletableFuture<Optional<VelocityTeam>>` | The team the player belongs to, if any. |
| `getTeam(teamId)` | `CompletableFuture<Optional<VelocityTeam>>` | Finds a team by UUID. |
| `getTeamByName(name)` | `CompletableFuture<Optional<VelocityTeam>>` | Finds a team by name. |
| `getTeamCount()` | `CompletableFuture<Integer>` | Total number of teams on the backend. |
| `teamExists(name)` | `CompletableFuture<Boolean>` | Whether a team with that name exists. |
| `isMember(teamId, playerUUID)` | `CompletableFuture<Boolean>` | Whether the player is a member of the team. |
| `getMemberRole(teamId, playerUUID)` | `CompletableFuture<Optional<VelocityTeamRole>>` | The player's role within the team, if a member. |
| `getMemberInfo(teamId, playerUUID)` | `CompletableFuture<Optional<VelocityTeamMember>>` | The player's full membership record, if a member. |
| `getAllTeams()` | `CompletableFuture<Collection<VelocityTeam>>` | All teams registered on the backend. |

### `VelocityTeam` (interface)

| Method | Returns | Description |
|--------|---------|-------------|
| `getId()` | `UUID` | Stable unique identifier for this team. |
| `getName()` | `String` | Internal team name. |
| `getDisplayName()` | `String` | Display name (may include formatting). |
| `getOwnerUUID()` | `UUID` | UUID of the team owner. |
| `getSize()` | `int` | Current member count. |
| `getMaxSize()` | `int` | Maximum member cap; `-1` means unlimited. |

### `VelocityTeamMember` (interface)

| Method | Returns | Description |
|--------|---------|-------------|
| `getPlayerUUID()` | `UUID` | The player's UUID. |
| `getRole()` | `VelocityTeamRole` | The role the player holds. |

### `VelocityTeamRole` (enum)

| Constant | Priority | Description |
|----------|----------|-------------|
| `OWNER` | 100 | Team owner. Full authority. |
| `ADMIN` | 50 | Team administrator. Can manage regular members. |
| `MEMBER` | 10 | Regular team member. |

## Multi-proxy setup (Redis)

By default the bridge routes every query through a player connected to **this** proxy.
When your network spans multiple Velocity instances, a player's backend may be reachable
only from a different proxy. Enabling Redis lets all proxies share queries over a
Pub/Sub channel so any proxy can fulfil a request for any player.

### How it works

```text
┌─────────────────────┐    teamsapi:request     ┌─────────────────────┐
│  Proxy A            │ ──────────────────────► │  Redis              │
│  (receives query)   │                          │  (Pub/Sub broker)   │
└─────────────────────┘ ◄────────────────────── └──────────┬──────────┘
         ▲               teamsapi:response:A               │
         │                                        teamsapi:request
         │                                                  │
         │                                                  ▼
         │                                       ┌─────────────────────┐
         │                                       │  Proxy B            │
         └── future resolved ◄─────────────────  │  (fulfils query via │
                                                  │   local player)     │
                                                  └─────────────────────┘
```text

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
```text

Set `redis.enabled: true` and point `host` / `port` at your Redis instance,
then restart the proxy. All Velocity proxies in the network must share the same
Redis instance and use identical `prefix` values.

### Requirements

- Redis 6+ (Redis 7 recommended)
- All proxies must use the same Redis instance and `prefix`
- All proxies must have `teams-api-velocity` installed with Redis enabled

## Limitations

- **Read-only**: `VelocityTeamsService` supports queries only. Mutations (create team,
  add member, etc.) must go through your backend plugin.
- **Route dependency (single proxy)**: when Redis is disabled, every query is routed
  through an online player on this proxy. If no player is connected to a backend,
  queries will fail with `IllegalStateException`.
- **Single backend per query**: the query is forwarded to whichever backend the routing
  player is on. Consistent multi-backend routing requires custom routing logic.
- **No invite/warp support**: the Velocity bridge currently covers `TeamsService`
  only. `TeamsInviteService` and `TeamsWarpService` are not bridged.

## See also

- [BungeeCord Guide](bungeecord): BungeeCord / Waterfall proxy bridge (same protocol)
- [Server Guide](server-guide): installing `teams-api-plugin` on a backend server
- [Developer Guide](developer-guide): full consumer guide for Bukkit plugins
- [API Reference](api): complete interface tables for all Bukkit-side types
