---
title: Server Guide
nav_order: 2
description: "Installation and setup guide for server owners running TeamsAPI"
---

# Server Guide
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

TeamsAPI is a **bridge plugin**. On its own it does nothing visible to players.
It installs a common interface that your team plugin and other plugins can use to
talk to each other, so you do not need to install separate compatibility add-ons
for every combination of plugins.

Think of it like Vault: install it once, then any plugin that reads economy or
permissions data will automatically work with whichever economy or permission
plugin you choose.

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | **17** or newer (Java 25 recommended) |
| Server software | Paper, Spigot, Purpur, or Folia — **1.16 or newer** |
| Team plugin | Any plugin that registers a `TeamsService` provider |

### Supported platforms

| Platform | Minimum version | Notes |
|----------|----------------|-------|
| **Paper** | 1.16 | Primary supported platform |
| **Spigot** | 1.16 | Fully compatible |
| **Purpur** | 1.16 | Fully compatible (Paper fork) |
| **Folia** | 1.16 | Fully compatible — no schedulers used |
| Velocity | any | Use the separate `teams-api-velocity` plugin (see [Velocity Guide](velocity)) |
| BungeeCord | any | Use the separate `teams-api-bungeecord` plugin (see [BungeeCord Guide](bungeecord)) |
| Fabric / Forge | — | Not supported; Bukkit API required |

## Installation

### Step 1: Download TeamsAPI

Download the latest `teams-api-plugin-VERSION.jar` from the
[Releases page](https://github.com/ez-plugins/teams-api/releases).

Make sure you download the **plugin JAR** (`teams-api-plugin-*.jar`), not the
API artifact (`teams-api-*.jar`). The API artifact is for developers only.

### Step 2: Place the JAR in your plugins folder

Copy `teams-api-plugin-VERSION.jar` into your server's `plugins/` directory.

```text
your-server/
  plugins/
    teams-api-plugin-1.3.0.jar   <-- add this
    YourTeamPlugin.jar
    ...
```

### Step 3: Install a compatible team plugin

TeamsAPI needs a team plugin that registers itself as a provider. Without one,
TeamsAPI loads successfully but has no data to serve. Check the documentation
of your team plugin to confirm it supports TeamsAPI.

### Step 4: (Re)start the server

Restart the server. TeamsAPI loads before your team plugin finishes enabling
(because team plugins declare `softdepend: TeamsAPI`), so no special startup
order is required.

## Verifying the installation

Look for log messages from your team plugin confirming that it has registered
with TeamsAPI. The exact wording depends on the team plugin, but you should see
something like:

```text
[YourTeamPlugin] Registered TeamsService with TeamsAPI.
```

If you see no such message and other plugins report that team features are
unavailable, check the troubleshooting section below.

You can also check which plugins are installed and enabled from the server
console:

```text
/plugins
```

Both `TeamsAPI` and your team plugin should appear green in the list.

## Optional services

Some team plugins register additional optional services beyond the core team
service:

| Service | What it provides |
|---------|-----------------|
| `TeamsInviteService` | Sending, accepting, and declining team invitations |
| `TeamsWarpService` | Creating and teleporting to named team warps |

These are independent of the core service. A plugin that reads warp data checks
whether a warp provider is registered and reports that warps are unsupported if
none is found. No configuration is required on your end; it is entirely up to
your team plugin whether it registers these services.

## Updating TeamsAPI

1. Stop the server.
2. Delete the old `teams-api-plugin-*.jar` from `plugins/`.
3. Place the new JAR in `plugins/`.
4. Start the server.

TeamsAPI has no configuration files and no stored data, so there is nothing to
migrate between versions.

Patch and minor version updates (for example 1.1.0 to 1.2.0) are always
backward-compatible. Existing team plugins and consumer plugins do not need to
be updated when you update TeamsAPI unless their own documentation says
otherwise.

## Troubleshooting

### TeamsAPI loads but no team features work

Your team plugin has not registered a provider. Possible causes:

- The team plugin does not support TeamsAPI. Check its documentation.
- The team plugin failed to enable. Check the server log for errors from that
  plugin.
- The team plugin registered at a later stage than expected. If the team plugin
  uses `Bukkit.getScheduler().runTaskLater(...)` to register, some consumer
  plugins may check availability before registration is complete. This is a bug
  in the team plugin, not in TeamsAPI.

### Invite or warp features are reported as unavailable

Not every team plugin implements `TeamsInviteService` or `TeamsWarpService`.
Check the documentation of your team plugin to see which services it registers.

### Two team plugins are installed

If two plugins both register as team providers, Bukkit's `ServicesManager` picks
the one with the higher priority. The active provider is the one returned by
`TeamsAPI.getService()`. Running two team plugins simultaneously is not a
supported configuration and may cause conflicts.

### Plugin version mismatch warnings

If a consumer plugin logs a warning about the API version, it was compiled
against a different version of TeamsAPI than the one installed. As long as the
major version number matches (for example, both are `1.x.x`), the plugins are
compatible. If the major version differs, you may need to update either TeamsAPI
or the consumer plugin.

## Frequently asked questions

**Do I need TeamsAPI if my team plugin already works?**

Only if another plugin you are installing requires it. TeamsAPI is a dependency,
not a standalone feature plugin.

**Can I use TeamsAPI without a team plugin?**

Yes. TeamsAPI will load and run without a team plugin present. Plugins that
depend on it will simply report that no team provider is available and disable
their team-related features.

**Does TeamsAPI add any commands or permissions?**

No. TeamsAPI has no player-facing commands, no permissions, and no configuration
file.

**Where is the config file?**

There is no config file. TeamsAPI requires no configuration.

## Velocity setup

TeamsAPI includes a separate Velocity plugin (`teams-api-velocity-VERSION.jar`) that
bridges team queries from proxy-side plugins to backend servers over the
`teamsapi:bridge` plugin-messaging channel.

### How the bridge works

```
Velocity plugin (consumer)
    └─ VelocityTeamsAPI.getService()
         └─ sends plugin message on teamsapi:bridge
              └─ backend Paper/Spigot server
                   └─ TeamsAPI plugin receives it
                        └─ calls TeamsAPI.getService()
                             └─ sends response back
```

All queries go through a connected player's server connection. **At least one
player must be online on the target backend server** for queries to route. For
networks with multiple Velocity proxies, enable the Redis bridge (see below) so
queries are automatically forwarded to whichever proxy can reach the target player.

### Installation

1. Download `teams-api-velocity-VERSION.jar` from the
   [Releases page](https://github.com/ez-plugins/teams-api/releases).
2. Drop it into the Velocity `plugins/` directory.
3. Drop the standard `teams-api-plugin-VERSION.jar` into the `plugins/` directory
   of every backend Paper/Spigot server.
4. Restart both Velocity and the backend servers.

No configuration is required for single-proxy setups.

### Multi-proxy (Redis)

If your network uses multiple Velocity instances, enable Redis in `config.yml`
(created on first startup in `plugins/teamsapi/config.yml`):

```yaml
redis:
  enabled: true
  host: "your-redis-host"
  port: 6379
  password: ""           # leave empty if no auth
  prefix: "teamsapi:"    # must match on all proxies
```

Every Velocity instance must point to the **same** Redis server and use the
same `prefix`. See the [Velocity Guide](velocity#multi-proxy-setup-redis) for details.

### Developer usage on Velocity

```java
if (!VelocityTeamsAPI.isAvailable()) {
    logger.warn("TeamsAPI bridge not active.");
    return;
}
VelocityTeamsService service = VelocityTeamsAPI.getService();
service.getPlayerTeam(playerUUID).thenAccept(opt ->
    opt.ifPresent(t -> logger.info("Team: " + t.getDisplayName())));
```

All service methods return `CompletableFuture<T>`. Futures complete exceptionally
with `TimeoutException` (5-second default) when no response arrives, or with
`IllegalStateException` when no online player is available to route the query.

## BungeeCord setup

TeamsAPI includes a separate BungeeCord plugin (`teams-api-bungeecord-VERSION.jar`) that
bridges team queries from proxy-side plugins to backend servers over the same
`teamsapi:bridge` plugin-messaging channel used by the Velocity bridge.

### How the bridge works

```
BungeeCord plugin (consumer)
    └─ BungeeTeamsAPI.getService()
         └─ sends plugin message on teamsapi:bridge
              └─ backend Paper/Spigot server
                   └─ TeamsAPI plugin receives it
                        └─ calls TeamsAPI.getService()
                             └─ sends response back
```

### Installation

1. Download `teams-api-bungeecord-VERSION.jar` from the
   [Releases page](https://github.com/ez-plugins/teams-api/releases).
2. Drop it into the BungeeCord / Waterfall `plugins/` directory.
3. Drop the standard `teams-api-plugin-VERSION.jar` into the `plugins/` directory
   of every backend server.
4. Restart both BungeeCord and the backend servers.

No configuration is required for single-proxy setups.

### Multi-proxy (Redis)

If your network uses multiple BungeeCord instances, enable Redis in `config.yml`
(created on first startup in `plugins/teamsapi/config.yml`):

```yaml
redis:
  enabled: true
  host: "your-redis-host"
  port: 6379
  password: ""           # leave empty if no auth
  prefix: "teamsapi:"    # must match on all proxies
```

Every BungeeCord instance must point to the **same** Redis server and use the
same `prefix`. See the [BungeeCord Guide](bungeecord#multi-proxy-setup-redis) for details.

### Developer usage on BungeeCord

```java
if (!BungeeTeamsAPI.isAvailable()) {
    getLogger().warning("TeamsAPI bridge not active.");
    return;
}
BungeeTeamsService service = BungeeTeamsAPI.getService();
service.getPlayerTeam(playerUUID).thenAccept(opt ->
    opt.ifPresent(t -> getLogger().info("Team: " + t.getDisplayName())));
```

For the full BungeeCord consumer guide, see the [BungeeCord Guide](bungeecord).
