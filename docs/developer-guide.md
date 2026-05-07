---
title: Developer Guide
nav_order: 3
has_children: true
description: "Architecture overview, installation instructions, and consumer usage guide"
---

# Developer Guide
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

TeamsAPI is a passive bridge plugin, modelled on the same design philosophy as
[Vault](https://github.com/MilkBowl/VaultAPI). It defines a standard interface
for team operations so that any plugin needing team data can work with any
compatible team plugin without coupling them together.

## Architecture

```text
┌───────────────────────────┐
│  Your plugin (consumer)   │  depends on  teams-api  only
└─────────────┬─────────────┘
              │  TeamsAPI.getService()
              ▼
┌───────────────────────────┐
│       TeamsAPI             │  installed on the server as TeamsAPI.jar
│  (static bridge facade)    │
└─────────────┬─────────────┘
              │  Bukkit ServicesManager
              ▼
┌───────────────────────────┐
│  Team plugin (provider)   │  e.g. Factions, Teams, or your own plugin
│  implements TeamsService  │
└───────────────────────────┘
```

Consumers depend only on the `teams-api` artifact and never import classes from
the team plugin directly. Providers register and unregister themselves through
`TeamsAPI.registerProvider(...)`.

The optional services (`TeamsInviteService` and `TeamsWarpService`) follow the
same pattern: each is registered and looked up independently from the core
service. A provider plugin can implement any combination of the three.

## Installation (server owners)

1. Download `teams-api-plugin-VERSION.jar` from the [Releases page](https://github.com/ez-plugins/teams-api/releases).
2. Place it in your server's `plugins/` directory.
3. Install a compatible team plugin that provides a `TeamsService` implementation.
4. Start or restart the server.

The plugin itself contains no game logic. It only bootstraps the Bukkit
`ServicesManager` bridge, so no configuration file is needed.

### Verifying the installation

When the server starts, any registered team plugin will log that it has
registered its services. If no team plugin is installed, consumers will receive
an empty `Optional` or `null` from the API and must handle that gracefully.

## For consumers

Consumers are plugins that read or react to team data. They depend on
`teams-api` but do not implement any service interfaces.

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
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>
```

Use `<scope>provided</scope>` because `teams-api` classes are supplied by the
`TeamsAPI.jar` on the server at runtime.

**Gradle**:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.2.0'
}
```

### 2. Declare the dependency in `plugin.yml`

```yaml
depend:
  - TeamsAPI
```

If team support is optional in your plugin, use `softdepend` instead. In that
case, always guard your API calls with `TeamsAPI.isAvailable()`.

### 3. Use the team service

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

### 4. Use the invite service (optional)

The invite service is registered separately from the core service. Always check
`TeamsAPI.isInviteAvailable()` before using it, as not every team plugin
implements invitations.

```java
private void handleInviteCommand(Player sender, Player target, UUID teamId) {
    if (!TeamsAPI.isInviteAvailable()) {
        sender.sendMessage("The active team plugin does not support invitations.");
        return;
    }
    TeamsInviteService invites = TeamsAPI.getInviteService();
    boolean sent = invites.invitePlayer(teamId, sender.getUniqueId(), target.getUniqueId());
    sender.sendMessage(sent ? "Invitation sent!" : "Could not send invitation.");
}
```

### 5. Use the warp service (optional)

The warp service is registered separately from the core service. Always check
`TeamsAPI.isWarpAvailable()` before using it, as not every team plugin
implements warps.

```java
private void handleWarpCommand(Player player, UUID teamId, String warpName) {
    if (!TeamsAPI.isWarpAvailable()) {
        player.sendMessage("The active team plugin does not support warps.");
        return;
    }
    TeamsWarpService warps = TeamsAPI.getWarpService();
    warps.getWarp(teamId, warpName).ifPresentOrElse(
        warp -> player.teleport(warp.getLocation()),
        () -> player.sendMessage("Warp '" + warpName + "' does not exist.")
    );
}
```

## Events

Providers are encouraged to fire events before performing state changes. Whether
they actually do so is implementation-specific; do not rely on events for
critical logic.

### Core events

All core events are cancellable.

| Event | When fired |
|-------|------------|
| `TeamCreateEvent` | Before a team is created |
| `TeamDeleteEvent` | Before a team is deleted |
| `TeamJoinEvent` | Before a player joins a team |
| `TeamLeaveEvent` | Before a player leaves a team |
| `TeamRoleChangeEvent` | Before a member's role changes |

### Invite events

Fired by providers that implement `TeamsInviteService`.

| Event | Cancellable | When fired |
|-------|-------------|------------|
| `TeamInviteEvent` | Yes | Before an invitation is recorded |
| `TeamInviteAcceptEvent` | No | After the player has joined the team |
| `TeamInviteDeclineEvent` | No | After the pending invitation was removed |

### Warp events

Fired by providers that implement `TeamsWarpService`.

| Event | Cancellable | When fired |
|-------|-------------|------------|
| `TeamWarpSetEvent` | Yes | Before a warp is created or updated |
| `TeamWarpDeleteEvent` | Yes | Before a warp is removed |

### Example listeners

```java
@EventHandler
public void onTeamJoin(TeamJoinEvent event) {
    if (event.getTeam().getSize() >= 10) {
        event.setCancelled(true);
    }
}

@EventHandler
public void onInvite(TeamInviteEvent event) {
    // Cancel to block the invitation
}

@EventHandler
public void onWarpSet(TeamWarpSetEvent event) {
    // Cancel to prevent the warp from being saved
}
```

## API versioning

Check `TeamsAPI.API_VERSION` at runtime if you need to guard against future
breaking changes:

```java
String version = TeamsAPI.API_VERSION; // e.g. "1.2.0"
```

TeamsAPI follows Semantic Versioning. A major version bump signals breaking
changes in `TeamsService` or the model interfaces. Minor bumps add optional,
backward-compatible features. Patch bumps are bug fixes only.

## See also

- [Team Provider](provider-teams): implementing `TeamsService` in your team plugin
- [Invite Provider](provider-invites): implementing `TeamsInviteService` for invitation support
- [Warp Provider](provider-warps): implementing `TeamsWarpService` for warp support
- [API Reference](api): interface and model overview
- [GitHub repository](https://github.com/ez-plugins/teams-api)
- [Jitpack page](https://jitpack.io/#ez-plugins/teams-api)
