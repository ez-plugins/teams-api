# TeamsAPI

**The universal bridge between team plugins and everything else.**

TeamsAPI is a passive, server-side bridge plugin for Paper, Spigot, Purpur, and Folia servers, inspired by Vault. It provides a single, stable interface for team operations, so any plugin that needs team data can work with any compatible team plugin, without either plugin knowing about the other.

## How it works

```text
Your Plugin (consumer)  ->  TeamsAPI (bridge)  ->  Team Plugin (provider)
```

- **Providers** -- faction, clan, guild, or custom team plugins `implement TeamsService`
  and register with TeamsAPI during `onEnable()`.
- **Consumers** -- scoreboard plugins, chat formatters, quest plugins, or any plugin that
  needs team data call `TeamsAPI.getService()` and use the returned interface.
- **Server owners** -- install `TeamsAPI.jar` and one compatible team plugin. Done.

No two plugins need to know about each other. When the team plugin changes, every
consumer plugin keeps working without a recompile.

## Features

- **Provider-agnostic**: works with any team plugin that ships a `TeamsService` implementation.
- **Graceful fallback**: if no provider is present, `TeamsAPI.isAvailable()` returns `false`; consumers can disable their team features cleanly instead of crashing.
- **Read-only snapshots**: `Team` and `TeamMember` are immutable interfaces; providers own the backing data.
- **Role hierarchy**: built-in `OWNER > ADMIN > MEMBER` with `outranks()` and `canManage()` helpers.
- **Optional invite service**: providers can expose `TeamsInviteService` for invitation workflows.
- **Optional warp service**: providers can expose `TeamsWarpService` for named team warps.
- **Cancellable events**: ten Bukkit events that providers can fire so other plugins can react to or cancel team operations.
- **Lightweight**: a single shaded JAR with no runtime dependencies beyond the Bukkit API.
- **JitPack-ready**: depend on just the API module at compile time; no transitive dependencies leak into your plugin.
- **Velocity bridge**: optional `teams-api-velocity` plugin for querying team data from the Velocity proxy. Supports multi-proxy networks via Redis.
- **BungeeCord bridge**: optional `teams-api-bungeecord` plugin for querying team data from BungeeCord / Waterfall proxies. Supports multi-proxy networks via Redis.

## Requirements

| Requirement | Value |
|-------------|-------|
| Server software | Paper / Spigot / Purpur / Folia 1.16+ |
| Java | 17+ (25 recommended) |
| Plugin dependencies | None |

## Installation (server owners)

1. Download `teams-api-plugin-VERSION.jar` from the **Files** tab of this listing or
   from [GitHub Releases](https://github.com/ez-plugins/teams-api/releases).
2. Drop it into your server's `plugins/` directory.
3. Install a compatible team plugin that provides a `TeamsService` implementation.
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
    <version>1.3.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.3.0'
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
