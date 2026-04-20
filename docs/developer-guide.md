---
title: Developer Guide
nav_order: 2
description: "Integration guide for providers (team plugins) and consumers"
---

# TeamsAPI — Developer Guide
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

TeamsAPI is a passive bridge plugin, modelled on the same design philosophy as
[Vault](https://github.com/MilkBowl/VaultAPI). It defines a standard interface
for team operations so that any plugin needing team data can work with any
compatible team plugin without coupling them together.

---

## Architecture

```text
┌───────────────────────────┐
│  Your plugin (consumer)   │  depends on  teams-api  only
└─────────────┬─────────────┘
              │  TeamsAPI.getService()
              ▼
┌───────────────────────────┐
│       TeamsAPI             │  ← installed on the server as TeamsAPI.jar
│  (static bridge facade)    │
└─────────────┬─────────────┘
              │  Bukkit ServicesManager
              ▼
┌───────────────────────────┐
│  Team plugin (provider)   │  e.g. Factions, Teams, or your own plugin
│  implements TeamsService  │
└───────────────────────────┘
```

Consumers depend only on the `teams-api` artifact — they never import classes
from the team plugin directly. Providers register and unregister themselves via
`TeamsAPI.registerProvider(...)`.

---

## Installation (server owners)

1. Download `teams-api-plugin-VERSION.jar` from the Releases page.
2. Place it in your server's `plugins/` directory.
3. Install a compatible team plugin that provides a `TeamsService` implementation.
4. Start or restart the server.

---

## For providers (team plugins)

### 1. Add the dependency

**Maven** (via Jitpack):

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
    <version>1.0.1</version>
    <scope>provided</scope>
</dependency>
```

**Gradle**:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.0.1'
}
```

### 2. Implement `TeamsService`

```java
public class MyTeamsService implements TeamsService {

    @Override
    public Optional<Team> createTeam(String name, UUID ownerUUID) {
        // Fire TeamCreateEvent before persisting
        // Return empty Optional if event is cancelled or name is taken
    }

    @Override
    public Optional<Team> getPlayerTeam(UUID playerUUID) {
        // Return the team this player belongs to
    }

    // ... implement all interface methods
}
```

### 3. Register and unregister

```java
private MyTeamsService teamsService;

@Override
public void onEnable() {
    teamsService = new MyTeamsService(this);
    TeamsAPI.registerProvider(this, teamsService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
}
```

### 4. Declare the soft-dependency in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

---

## For consumers (plugins using team data)

### 1. Add the dependency (same as providers above)

Use `scope: provided` in Maven or `compileOnly` in Gradle since `teams-api`
classes are provided by the `TeamsAPI.jar` installed on the server.

### 2. Declare the dependency in `plugin.yml`

```yaml
depend:
  - TeamsAPI
```

If team support is optional in your plugin, use `softdepend` instead.

### 3. Use the API

```java
@Override
public void onEnable() {
    if (!TeamsAPI.isAvailable()) {
        getLogger().warning("No team plugin found — team features disabled.");
        return;
    }
    getLogger().info("TeamsAPI found: team features enabled.");
}

// Later, in a command or listener:
private void handlePlayerCommand(Player player) {
    TeamsService teams = TeamsAPI.getService();
    if (teams == null) {
        player.sendMessage("Team features are not available on this server.");
        return;
    }

    Optional<Team> team = teams.getPlayerTeam(player.getUniqueId());
    if (team.isEmpty()) {
        player.sendMessage("You are not in a team.");
        return;
    }

    player.sendMessage("Your team: " + team.get().getDisplayName());
    player.sendMessage("Members: " + team.get().getSize());
}
```

---

## Events

All events are in the `com.skyblockexp.teamsapi.event` package.
Providers are **encouraged** to fire these events; whether they do is
implementation-specific.

| Event                  | When fired                          | Cancellable |
|------------------------|-------------------------------------|-------------|
| `TeamCreateEvent`      | Before a team is created            | Yes         |
| `TeamDeleteEvent`      | Before a team is deleted            | Yes         |
| `TeamJoinEvent`        | Before a player joins a team        | Yes         |
| `TeamLeaveEvent`       | Before a player leaves a team       | Yes         |
| `TeamRoleChangeEvent`  | Before a member's role changes      | Yes         |

Example listener:

```java
@EventHandler
public void onTeamJoin(TeamJoinEvent event) {
    Team team = event.getTeam();
    UUID player = event.getPlayerUUID();

    if (team.getSize() >= 10) {
        event.setCancelled(true);
        // Notify player that the team is full
    }
}
```

---

## API versioning

Check `TeamsAPI.API_VERSION` at runtime if you need to guard against future
breaking changes:

```java
String version = TeamsAPI.API_VERSION; // e.g. "1.0.0"
```

TeamsAPI follows Semantic Versioning. A major version bump indicates breaking
changes in `TeamsService` or the model interfaces.

---

## See Also

- [API Reference](./api.md) — interface and model overview
- [GitHub repository](https://github.com/ez-plugins/teams-api)
- [Jitpack page](https://jitpack.io/#ez-plugins/teams-api)
