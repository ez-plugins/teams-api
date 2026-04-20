# TeamsAPI

**The universal bridge between team plugins and everything else.**

TeamsAPI is a passive, server-side bridge plugin for Paper servers, inspired by
[Vault](https://github.com/MilkBowl/VaultAPI). It provides a single, stable
interface for team operations — so any plugin that needs team data can work with
any compatible team plugin, without either plugin knowing about the other.

---

## How it works

```
Your Plugin (consumer)  →  TeamsAPI (bridge)  →  Team Plugin (provider)
```

- **Providers** — faction, clan, guild, or custom team plugins `implement TeamsService`
  and register with TeamsAPI during `onEnable()`.
- **Consumers** — scoreboard plugins, chat formatters, quest plugins, or any plugin that
  needs team data call `TeamsAPI.getService()` and use the returned interface.
- **Server owners** — install `TeamsAPI.jar` and one compatible team plugin. Done.

No two plugins need to know about each other. When the team plugin changes, every
consumer plugin keeps working without a recompile.

---

## Features

- **Provider-agnostic** — works with any team plugin that ships a `TeamsService` implementation.
- **Graceful fallback** — if no provider is present, `TeamsAPI.isAvailable()` returns `false`;
  consumers can disable their team features cleanly instead of crashing.
- **Read-only snapshots** — `Team` and `TeamMember` are immutable interfaces; providers own
  the backing data.
- **Role hierarchy** — built-in `OWNER › ADMIN › MEMBER` with `outranks()` and
  `canManage()` helpers.
- **Cancellable events** — five Bukkit events that providers can fire so other plugins can
  react to or cancel team operations.
- **Lightweight** — a single shaded JAR with no runtime dependencies beyond Paper.
- **JitPack-ready** — depend on just the API module at compile time; no transitive dependencies leak into your plugin.

---

## Requirements

| Requirement       | Value      |
|-------------------|------------|
| Server software   | Paper 26.1+ |
| Java              | 25+        |
| Plugin dependencies | None     |

---

## Installation (server owners)

1. Download `teams-api-plugin-VERSION.jar` from the **Files** tab of this listing or
   from [GitHub Releases](https://github.com/ez-plugins/teams-api/releases).
2. Drop it into your server's `plugins/` directory.
3. Install a compatible team plugin that provides a `TeamsService` implementation.
4. Restart the server.

TeamsAPI has no configuration files. The only command is `/teamsapi version` (requires
`teamsapi.admin`).

---

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
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.0.0'
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
        getLogger().warning("No team plugin found — team features disabled.");
        return;
    }
    getLogger().info("TeamsAPI found: team features enabled.");
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

---

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

### Events

All events are cancellable and live in `com.skyblockexp.teamsapi.event`.
Providers are encouraged (but not required) to fire them.

| Event | Fired when |
|-------|-----------|
| `TeamCreateEvent` | Before a team is created |
| `TeamDeleteEvent` | Before a team is deleted |
| `TeamJoinEvent` | Before a player joins a team |
| `TeamLeaveEvent` | Before a player leaves a team |
| `TeamRoleChangeEvent` | Before a member's role changes |

### Roles

| Role | Priority | Description |
|------|----------|-------------|
| `OWNER` | 100 | Full control; cannot be removed by others |
| `ADMIN` | 50 | Can manage members with a lower priority |
| `MEMBER` | 10 | Regular team member |

---

## Links

- [GitHub](https://github.com/ez-plugins/teams-api) — source code & issue tracker
- [Developer Guide](https://ez-plugins.github.io/teams-api/developer-guide.html) — full integration walkthrough
- [API Reference](https://ez-plugins.github.io/teams-api/api.html) — complete method tables
- [JitPack](https://jitpack.io/#ez-plugins/teams-api) — Maven / Gradle dependency

---

*MIT License — free to use in any project, open- or closed-source.*
