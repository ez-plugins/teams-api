[![CI](https://github.com/ez-plugins/teams-api/actions/workflows/ci.yml/badge.svg)](https://github.com/ez-plugins/teams-api/actions)
[![codecov](https://codecov.io/gh/ez-plugins/teams-api/branch/main/graph/badge.svg)](https://codecov.io/gh/ez-plugins/teams-api)
[![License](https://img.shields.io/github/license/ez-plugins/teams-api)](LICENSE)
[![Jitpack](https://jitpack.io/v/ez-plugins/teams-api.svg)](https://jitpack.io/#ez-plugins/teams-api)

**The universal bridge between team plugins and everything else.**

TeamsAPI is a passive, server-side bridge plugin for Paper, Spigot, Purpur, and Folia servers, inspired by Vault. It provides a single, stable interface for team operations, so any plugin that needs team data can work with any compatible team plugin, without either plugin knowing about the other.

Implemented in our refactored Factions fork: [https://modrinth.com/plugin/pvpindex-factions](https://modrinth.com/plugin/pvpindex-factions)

## How it works

![Teams API connect your plugin with team plugins](https://i.ibb.co/VpzgC9SK/teams-api-header.png)

- **Providers** -- faction, clan, guild, or custom team plugins `implement TeamsService`
  and register with TeamsAPI during `onEnable()`.
- **Consumers** -- scoreboard plugins, chat formatters, quest plugins, or any plugin that
  needs team data call `TeamsAPI.getService()` and use the returned interface.
- **Server owners** -- install `TeamsAPI.jar` and one compatible team plugin. Done.

No two plugins need to know about each other. When the team plugin changes, every
consumer plugin keeps working without a recompile.

## Features

- **Official extensions**: ready-to-use provider bridges for [BetterTeams](https://www.spigotmc.org/resources/betterteams.17664/), [Towny Advanced](https://modrinth.com/plugin/towny), and [KingdomsX](https://www.spigotmc.org/resources/kingdomsx.77782/), bundled inside the main JAR. Install in-game with `/teamsapi install <name>`.
- **Provider-agnostic**: works with any team plugin that ships a `TeamsService` implementation.
- **Graceful fallback**: if no provider is present, `TeamsAPI.isAvailable()` returns `false`; consumers can disable their team features cleanly instead of crashing.
- **Read-only snapshots**: `Team` and `TeamMember` are immutable interfaces; providers own the backing data.
- **Role hierarchy**: built-in `OWNER > ADMIN > MEMBER` with `outranks()` and `canManage()` helpers.
- **Optional invite service**: providers can expose `TeamsInviteService` for invitation workflows.
- **Optional warp service**: providers can expose `TeamsWarpService` for named team warps.
- **Optional claim service**: providers can expose `TeamsClaimService` for chunk-claim management, including SafeZone and WarZone territory support.
- **Optional power service**: providers can expose `TeamsPowerService` for player and team power values.
- **Optional power-history service**: providers can expose `TeamsPowerHistoryService`
  for reading and managing player/team power-history entries.
- **Optional relation service**: providers can expose `TeamsRelationService` for inter-team diplomacy (ally/truce/neutral/enemy).
- **Optional notification service**: providers can expose `TeamsNotificationService` for
  cross-plugin player notifications using built-in enum types and custom string types.
- **Custom subcommands**: any plugin registers a `TeamsSubcommand` via `TeamsAPI.registerSubcommand()`; team plugins call `TeamsAPI.getSubcommands()` in their own command handler to dispatch them, extending the command tree without coupling.
- **Cancellable events**: fifteen Bukkit events that providers can fire so other plugins can react to or cancel team operations.
- **Lightweight**: a single shaded JAR with no required runtime dependencies beyond the Bukkit API (optional: [Vault](https://github.com/MilkBowl/VaultAPI) for the built-in power shop).
- **JitPack-ready**: depend on just the API module at compile time; no transitive dependencies leak into your plugin.
- **Velocity bridge** *(experimental)*: optional `teams-api-velocity` plugin for querying team data from the Velocity proxy. Supports multi-proxy networks via Redis.
- **BungeeCord bridge** *(experimental)*: optional `teams-api-bungeecord` plugin for querying team data from BungeeCord / Waterfall proxies. Supports multi-proxy networks via Redis.

## Requirements

| Requirement | Value |
|-------------|-------|
| Server software | Paper / Spigot / Purpur / Folia 1.16+ |
| Java | 17+ (25 recommended) |
| Plugin dependencies | None (optional: [Vault](https://github.com/MilkBowl/VaultAPI) for power shop) |

## Installation (server owners)

1. Download `teams-api-plugin-VERSION.jar` from the **Files** tab of this listing or
   from [GitHub Releases](https://github.com/ez-plugins/teams-api/releases).
2. Drop it into your server's `plugins/` directory.
3. Install a compatible team plugin. Official extensions for
   [BetterTeams](https://www.spigotmc.org/resources/betterteams.17664/),
   [Towny Advanced](https://modrinth.com/plugin/towny), and
   [KingdomsX](https://www.spigotmc.org/resources/kingdomsx.77782/) are bundled
   inside TeamsAPI. Run `/teamsapi install betterteams` (or `towny` / `kingdomsx`)
   in-game, or let TeamsAPI provision them automatically to `plugins/TeamsAPI/extensions/`.
4. Restart the server.

TeamsAPI has no configuration files.

## For developers

Add the API artifact to your project via [JitPack](https://jitpack.io/#ez-plugins/teams-api):

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ez-plugins</groupId>
    <artifactId>teams-api</artifactId>
    <version>2.2.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:2.2.0'
}
```

### Consumer quick-start

Declare the dependency in `plugin.yml` (use `softdepend` if team support is optional):

```yaml
depend:
  - TeamsAPI
```

Then use the API at runtime:

```java
@Override
public void onEnable() {
    if (!TeamsAPI.isAvailable()) {
        getLogger().warning("No team plugin found. Team features disabled.");
        return;
    }
    getLogger().info("TeamsAPI found. Team features enabled.");
}

// In a command or listener:
TeamsService teams = TeamsAPI.getService();
Optional<Team> team = teams.getPlayerTeam(player.getUniqueId());
team.ifPresent(t -> player.sendMessage("Your team: " + t.getDisplayName()));
```

### Provider quick-start

Declare a soft-dependency in `plugin.yml`:

```yaml
softdepend:
  - TeamsAPI
```

Register your implementation when the plugin loads:

```java
@Override
public void onEnable() {
    TeamsAPI.registerProvider(this, new MyTeamsService(this));
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
}
```

## API surface

### Team lifecycle & lookup

| Method | Returns | Description |
|--------|---------|-------------|
| `createTeam(name, ownerUUID)` | `Optional<Team>` | Creates a new team |
| `deleteTeam(teamId)` | `boolean` | Deletes a team by UUID |
| `getTeam(teamId)` | `Optional<Team>` | Looks up a team by UUID |
| `getTeamByName(name)` | `Optional<Team>` | Looks up a team by name |
| `getPlayerTeam(playerUUID)` | `Optional<Team>` | Returns the player's current team |
| `getAllTeams()` | `Collection<Team>` | Returns every registered team |
| `getTeamCount()` | `int` | Total number of teams |

### Membership management

| Method | Returns | Description |
|--------|---------|-------------|
| `addMember(teamId, playerUUID, role)` | `boolean` | Adds a player with a given role |
| `removeMember(teamId, playerUUID)` | `boolean` | Removes a player from the team |
| `setMemberRole(teamId, playerUUID, role)` | `boolean` | Changes a member's role |
| `getMemberRole(teamId, playerUUID)` | `Optional<TeamRole>` | Returns the member's current role |

### Invite service (optional)

Register alongside `TeamsService` if your plugin supports invitations:

```java
TeamsAPI.registerInviteProvider(this, inviteService);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `invitePlayer(teamId, inviterUUID, inviteeUUID)` | `boolean` | Sends an invitation |
| `acceptInvite(teamId, playerUUID)` | `Optional<Team>` | Accepts the invitation and joins the team |
| `declineInvite(teamId, playerUUID)` | `boolean` | Declines an invitation |

Consumers check availability with `TeamsAPI.isInviteAvailable()` before calling `TeamsAPI.getInviteService()`.

### Warp service (optional)

Register alongside `TeamsService` if your plugin supports team warps:

```java
TeamsAPI.registerWarpProvider(this, warpService);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `setWarp(teamId, name, location, creatorUUID)` | `boolean` | Creates or updates a named warp |
| `removeWarp(teamId, name)` | `boolean` | Deletes a warp by name |
| `getWarp(teamId, name)` | `Optional<TeamWarp>` | Looks up a warp by name |
| `getWarps(teamId)` | `Collection<TeamWarp>` | Returns all warps for a team |

Consumers check availability with `TeamsAPI.isWarpAvailable()` before calling `TeamsAPI.getWarpService()`.

### Claim service (optional)

Register alongside `TeamsService` if your plugin supports chunk claims:

```java
TeamsAPI.registerClaimProvider(this, claimService);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `claimChunk(teamId, playerUUID, world, x, z)` | `boolean` | Claims a chunk for the team |
| `unclaimChunk(teamId, playerUUID, world, x, z)` | `boolean` | Removes the claim |
| `unclaimAll(teamId)` | `boolean` | Removes all claims for the team |
| `getClaimAt(world, x, z)` | `Optional<TeamClaim>` | Returns the claim at a chunk, if any |
| `getTeamClaims(teamId)` | `Collection<TeamClaim>` | All claims for the team |
| `getClaimCount(teamId)` | `int` | Number of claimed chunks |
| `isClaimed(world, x, z)` | `boolean` | Whether the chunk is claimed by anyone |
| `isClaimedBy(teamId, world, x, z)` | `boolean` | Whether the chunk is claimed by this team |
| `getTerritoryTypeAt(world, x, z)` | `ClaimTerritoryType` | Returns `WILDERNESS`, `TEAM`, `SAFE_ZONE`, or `WAR_ZONE` |
| `isSafeZone(world, x, z)` | `boolean` | Whether the chunk is a SafeZone |
| `isWarZone(world, x, z)` | `boolean` | Whether the chunk is a WarZone |
| `getTeamMaxClaims(teamId)` | `int` | Claim limit (-1 means unlimited) |

Consumers check availability with `TeamsAPI.isClaimAvailable()` before calling `TeamsAPI.getClaimService()`.

### Power service (optional)

Register alongside `TeamsService` if your plugin exposes power values:

```java
TeamsAPI.registerPowerProvider(this, powerService);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `getPlayerPower(playerUUID)` | `double` | Current power of the player |
| `getPlayerMaxPower(playerUUID)` | `double` | Maximum power the player can hold |
| `setPlayerPower(playerUUID, power)` | `boolean` | Sets the player's power |
| `getTeamPower(teamId)` | `double` | Combined power of all team members |
| `getTeamMaxPower(teamId)` | `double` | Maximum combined power the team can hold |

Consumers check availability with `TeamsAPI.isPowerAvailable()` before calling `TeamsAPI.getPowerService()`.

### Power history service (optional)

Register alongside `TeamsService` if your plugin exposes power history:

```java
TeamsAPI.registerPowerHistoryProvider(this, powerHistoryService);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `getPlayerPowerHistory(playerUUID, limit)` | `Collection<TeamPowerHistoryEntry>` | Recent entries for a player, newest first. |
| `getPlayerPowerHistory(playerUUID, fromInclusive, toExclusive, limit)` | `Collection<TeamPowerHistoryEntry>` | Player entries in a time window, newest first. |
| `getTeamPowerHistory(teamId, limit)` | `Collection<TeamPowerHistoryEntry>` | Recent entries linked to a team. |
| `addPowerHistoryEntry(entryId, playerUUID, teamId, delta, type, reason, actorUUID, occurredAt, details)` | `boolean` | Inserts a history entry. |
| `updatePowerHistoryEntry(entryId, delta, type, reason, actorUUID, occurredAt, details)` | `boolean` | Updates an existing entry. |
| `removePowerHistoryEntry(entryId)` | `boolean` | Deletes one entry by ID. |
| `clearPlayerPowerHistory(playerUUID)` | `int` | Deletes all player entries; returns removed count. |
| `clearTeamPowerHistory(teamId)` | `int` | Deletes all team-linked entries; returns removed count. |

Consumers check availability with `TeamsAPI.isPowerHistoryAvailable()` before calling
`TeamsAPI.getPowerHistoryService()`.

### Relation service (optional)

Register alongside `TeamsService` if your plugin supports inter-team diplomacy:

```java
TeamsAPI.registerRelationProvider(this, relationService);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `setRelation(fromTeamId, toTeamId, relation, initiatorUUID)` | `boolean` | Declares a relation from one team toward another. Fires `TeamRelationChangeEvent`; returns `false` if cancelled. Setting `NEUTRAL` removes the relation. |
| `getRelation(fromTeamId, toTeamId)` | `TeamRelation` | Returns the declared relation (defaults to `NEUTRAL` if none is set). |
| `getRelations(teamId)` | `Map<UUID, TeamRelation>` | All non-neutral relations declared by the team. |
| `clearRelations(teamId)` | `boolean` | Removes all relations declared by or toward the team (use on disband). |
| `areAllies(teamAId, teamBId)` | `boolean` | Returns `true` when both teams have declared `ALLY` toward each other. |
| `areEnemies(teamAId, teamBId)` | `boolean` | Returns `true` when either team has declared `ENEMY` toward the other. |
| `getTeamsInRelation(teamId, relation)` | `Collection<UUID>` | Returns all team UUIDs toward which `teamId` has declared the given relation. Filters `getRelations(teamId)` by value; providers may override for efficiency. |
| `getRelationColor(relation)` | `String` | `#RRGGBB` hex color for the relation. Default returns `relation.getDefaultHexColor()`; providers may override to supply server-configured colors. |

`TeamRelation` values with display name, legacy color code, and hex color:

| Constant | Display name | Legacy color | Hex color |
|----------|-------------|--------------|-----------|
| `ALLY`    | "Ally"    | `§a` (green) | `#55FF55` |
| `TRUCE`   | "Truce"   | `§6` (gold)  | `#FFAA00` |
| `NEUTRAL` | "Neutral" | `§7` (gray)  | `#AAAAAA` |
| `ENEMY`   | "Enemy"   | `§c` (red)   | `#FF5555` |

Consumers check availability with `TeamsAPI.isRelationAvailable()` before calling `TeamsAPI.getRelationService()`.

### Notification service (optional)

Register alongside `TeamsService` if your plugin supports cross-plugin player notifications:

```java
TeamsAPI.registerNotificationProvider(this, notificationService);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `sendNotification(senderPlugin, recipientUUID, type, message)` | `boolean` | Sends a notification using built-in `TeamNotificationType`. |
| `sendNotification(senderPlugin, recipientUUID, notificationType, message)` | `boolean` | Sends a notification using a custom string type (non-null, non-blank). |
| `isNotificationEnabled(playerUUID, type)` | `boolean` | Whether this built-in notification type is enabled for the player. |
| `isNotificationEnabled(playerUUID, notificationType)` | `boolean` | Whether this custom string notification type is enabled for the player. |
| `setNotificationEnabled(playerUUID, type, enabled)` | `boolean` | Enables or disables a built-in notification type for the player. |
| `setNotificationEnabled(playerUUID, notificationType, enabled)` | `boolean` | Enables or disables a custom string notification type for the player. |

Built-in `TeamNotificationType` values:
`GENERAL`, `TEAM_JOIN`, `TEAM_LEAVE`, `TEAM_INVITE`,
`TEAM_INVITE_ACCEPT`, `TEAM_INVITE_DECLINE`.

Consumers check availability with `TeamsAPI.isNotificationAvailable()` before calling
`TeamsAPI.getNotificationService()`.

### Custom subcommands

Any plugin can register a `TeamsSubcommand` via `TeamsAPI.registerSubcommand()`. Team
plugins call `TeamsAPI.getSubcommands()` in their own command executor to dispatch them,
extending the command tree without any direct coupling between plugins.

Register in `onEnable`:

```java
TeamsAPI.registerSubcommand(this, new MySubcommand());
```

Unregister in `onDisable` (Bukkit's ServicesManager also handles this automatically
on plugin unload):

```java
TeamsAPI.unregisterSubcommand(mySubcommand);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `getName()` | `String` | Matched case-insensitively against `args[0]` |
| `getDescription()` | `String` | Optional description for help output |
| `getPermission()` | `String` | Permission required, or `null` for no check |
| `execute(sender, args)` | `boolean` | Called when the subcommand is dispatched; return `false` to show usage |
| `getUsage()` | `String` | Usage hint sent when `execute` returns `false` |
| `tabComplete(sender, args)` | `List<String>` | Tab-completion suggestions; default: empty list |

### Commands & permissions

| Subcommand | Permission | Default | Description |
|------------|-----------|---------|-------------|
| `/teamsapi` / `/teamsapi help` | `teamsapi.use` | everyone | Lists commands the sender can use |
| `/teamsapi version` | `teamsapi.use` | everyone | Prints plugin and API version |
| `/teamsapi status` | `teamsapi.status` | everyone | Active provider, team count, registered services |
| `/teamsapi info` | `teamsapi.admin` | op | Full internal diagnostic |
| `/teamsapi power status` | `teamsapi.power` | op | Sender's current and max power |
| `/teamsapi power buy <n>` | `teamsapi.power.buy` | disabled | Disabled by default; enable with `power-shop.enabled: true` in `config.yml`. Requires Vault. |
| `/teamsapi install <extension>` | `teamsapi.install` | op | Copies a bundled extension JAR to `plugins/TeamsAPI/extensions/`. Valid names: `betterteams`, `towny`, `kingdomsx`. |
| `/teamsapi load <file>.jar` | `teamsapi.load` | op | Loads and enables an extension from `plugins/TeamsAPI/extensions/` without a server restart. |

### Events

All events live in `com.skyblockexp.teamsapi.event`. Providers are encouraged but not required to fire them.

| Event | Cancellable | Fired when |
|-------|-------------|------------|
| `TeamCreateEvent` | Yes | Before a team is created |
| `TeamDeleteEvent` | Yes | Before a team is deleted |
| `TeamJoinEvent` | Yes | Before a player joins a team |
| `TeamLeaveEvent` | Yes | Before a player leaves a team |
| `TeamRoleChangeEvent` | Yes | Before a member's role changes |
| `TeamInviteEvent` | Yes | Before an invitation is sent |
| `TeamInviteAcceptEvent` | No | After a player accepts an invitation |
| `TeamInviteDeclineEvent` | No | After a player declines an invitation |
| `TeamWarpSetEvent` | Yes | Before a warp is created or updated |
| `TeamWarpDeleteEvent` | Yes | Before a warp is deleted |
| `TeamClaimEvent` | Yes | Before a chunk is claimed |
| `TeamUnclaimEvent` | Yes | Before a chunk is unclaimed |

### Roles

| Role | Priority | Description |
|------|----------|-------------|
| `OWNER` | 100 | Full control; cannot be removed by others |
| `ADMIN` | 50 | Can manage members with a lower priority |
| `MEMBER` | 10 | Regular team member |

## Links

- [GitHub](https://github.com/ez-plugins/teams-api) -- source code & issue tracker
- [Developer Guide](https://ez-plugins.github.io/teams-api/developer-guide.html) -- full integration walkthrough
- [API Reference](https://ez-plugins.github.io/teams-api/api.html) -- complete method tables
- [JitPack](https://jitpack.io/#ez-plugins/teams-api) -- Maven / Gradle dependency

---

*MIT License - free to use in any project, open- or closed-source.*
