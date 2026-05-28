---
title: Developer Guide
nav_order: 6
description: "Architecture and integration concepts for TeamsAPI"
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

Audience shortcuts:

- Addon plugin developers: [Consumer Guide](consumer-guide)
- Team plugin developers: [Provider Guide](provider-guide)

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

The optional services (`TeamsInviteService`, `TeamsWarpService`, `TeamsChestService`,
`TeamsClaimService`, and `TeamsPowerService`) follow the same pattern: each is registered and looked up
independently from the core service. A provider plugin can implement any combination.

## Repository layout

The repository contains several Maven modules, each with a distinct role:

```text
teams-api/                          # API artifact — the only compile-time dependency
  src/main/java/
    com/skyblockexp/teamsapi/
      api/                          # TeamsAPI facade + all service interfaces
      model/                        # Team, TeamMember, TeamRole, TeamRelation, …
      event/                        # TeamEvent base + all concrete event classes
  src/test/java/                    # Unit tests for the API module

teams-api-plugin/                   # Bukkit plugin — ships as TeamsAPI.jar
  src/main/java/
    TeamsApiPlugin.java             # Plugin entry point (passive bootstrap only)
  src/main/resources/
    plugin.yml                      # Declares /teamsapi command
    extensions/                     # Bundled extension JARs (provisioned on startup)

teams-api-extension-betterteams/    # Provider bridge for BetterTeams
teams-api-extension-towny/          # Provider bridge for Towny Advanced
teams-api-extension-kingdomsx/      # Provider bridge for KingdomsX

teams-api-velocity/                 # Velocity proxy bridge (experimental)
teams-api-bungeecord/               # BungeeCord/Waterfall proxy bridge (experimental)

docs/                               # Jekyll-based documentation site
  consumer-guide.md                 # Entry point for consumer plugin developers
  consumer-tutorial-bukkit.md       # Step-by-step Bukkit consumer tutorial
  consumer-roles.md                 # Working with TeamRole: reading, comparing, prefixes
  provider-guide.md                 # Entry point for team plugin developers
  provider-tutorial.md              # Step-by-step provider tutorial
  api.md                            # Complete API reference (all interfaces and enums)
  developer-guide.md                # Architecture overview (this file)

listing/                            # Marketplace listing descriptions
  modrinth-hangar.md                # Modrinth / Hangar listing (Markdown)
  spigotmc.bbcode                   # SpigotMC listing (BBCode)

libs/                               # Test-fixture JARs used in CI smoke tests
```

### Package roles

| Package | Purpose |
|---------|---------|
| `api`   | Public entry-points (`TeamsAPI` facade, all `Teams*Service` interfaces). No implementation logic. |
| `model` | Read-only data interfaces and enums. No Bukkit state. |
| `event` | Bukkit event classes. Each concrete event owns its `HandlerList`. |

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

If you want a full copy-paste starter first, use:

- [Consumer Tutorial (Bukkit)](consumer-tutorial-bukkit)
- [Consumer Tutorial (Proxy)](consumer-tutorial-proxy)
- [Consumer Recipe Dropdown](consumer-tutorial-bukkit#consumer-tutorial-dropdown-recipes)

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
    <version>1.7.0</version>
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
    compileOnly 'com.github.ez-plugins:teams-api:1.7.0'
}
```

### 2. Declare the dependency in `plugin.yml`

```yaml
depend:
  - TeamsAPI
```

If team support is optional in your plugin, use `softdepend` instead. In that
case, always guard your API calls with `TeamsAPI.isAvailable()`.

Recommended `softdepend` setup:

```yaml
softdepend:
  - TeamsAPI
```

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

Consumer safety checklist:

- Treat `TeamsAPI.getService()` as nullable every time you read it.
- Treat every `Optional<T>` lookup as potentially empty.
- Use user-facing fallback messages instead of stack traces for unavailable services.
- Keep all optional service usage behind `TeamsAPI.isXAvailable()` checks.
- Avoid caching provider-owned objects long-term; re-query when needed.

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

### 6. Use the claim service (optional)

### 6. Use the chest service (optional)

The chest service is registered separately from the core service. Always check
`TeamsAPI.isChestAvailable()` before using it.

```java
private void handleChestSummary(Player player, UUID teamId, ItemStack sample) {
    if (!TeamsAPI.isChestAvailable()) {
        player.sendMessage("The active team plugin does not support team chests.");
        return;
    }
    TeamsChestService chests = TeamsAPI.getChestService();
    Collection<String> chestIds = chests.getChestIds(teamId);
    Collection<ItemStack> contents = chests.getContents(teamId, "vault");
    boolean replaced = chests.setContents(teamId, "vault", contents);
    boolean added = chests.addItem(teamId, "vault", sample);
    player.sendMessage("Chests: " + chestIds.size() + ", vault items: " + contents.size()
        + " (replace result: " + replaced + ", add result: " + added + ")");
}
```

### 7. Use the claim service (optional)

The claim service is registered separately from the core service. Always check
`TeamsAPI.isClaimAvailable()` before using it.

```java
private void handleClaimCommand(Player player, UUID teamId) {
    if (!TeamsAPI.isClaimAvailable()) {
        player.sendMessage("The active team plugin does not support claims.");
        return;
    }
    TeamsClaimService claims = TeamsAPI.getClaimService();
    Chunk chunk = player.getLocation().getChunk();
    int max = claims.getTeamMaxClaims(teamId);
    int current = claims.getClaimCount(teamId);
    if (max != -1 && current >= max) {
        player.sendMessage("Your team has reached its claim limit (" + max + ").");
        return;
    }
    boolean ok = claims.claimChunk(
        teamId, player.getUniqueId(),
        chunk.getWorld().getName(), chunk.getX(), chunk.getZ()
    );
    player.sendMessage(ok ? "Chunk claimed!" : "That chunk is already claimed.");
}
```

For SafeZone / WarZone-aware providers, consumers can read territory type:

```java
ClaimTerritoryType type = claims.getTerritoryTypeAt(
    chunk.getWorld().getName(), chunk.getX(), chunk.getZ()
);
if (type == ClaimTerritoryType.WAR_ZONE) {
    player.sendMessage("PvP is always enabled here.");
}
```

### 8. Use the power service (optional)

The power service is registered separately from the core service. Always check
`TeamsAPI.isPowerAvailable()` before using it.

```java
private void handlePowerCommand(Player player, UUID teamId) {
    if (!TeamsAPI.isPowerAvailable()) {
        player.sendMessage("The active team plugin does not expose power values.");
        return;
    }
    TeamsPowerService power = TeamsAPI.getPowerService();
    double current = power.getTeamPower(teamId);
    double max = power.getTeamMaxPower(teamId);
    player.sendMessage("Team power: " + current + " / " + max);
}
```

### 9. Use the relation service (optional)

The relation service is registered separately from the core service. Always check
`TeamsAPI.isRelationAvailable()` before using it.

```java
private void handleRelationQuery(Player player, UUID myTeamId, UUID theirTeamId) {
    if (!TeamsAPI.isRelationAvailable()) {
        player.sendMessage("The active team plugin does not support relations.");
        return;
    }
    TeamsRelationService relations = TeamsAPI.getRelationService();
    TeamRelation rel = relations.getRelation(myTeamId, theirTeamId);
    String color = relations.getRelationColor(rel);
    player.sendMessage(rel.getDisplayName() + " (" + color + ")");

    if (relations.areAllies(myTeamId, theirTeamId)) {
        player.sendMessage("You are mutual allies!");
    }
}
```

### 10. Register a custom subcommand (providers)

Providers can expose additional commands under `/teamsapi <name>` without
shipping a separate Bukkit command. See the dedicated
[Custom Subcommands](provider-subcommands) guide for the full walkthrough,
permission behaviour, and a complete working example.

Quick-start:

```java
@Override
public void onEnable() {
    TeamsAPI.registerSubcommand(this, new StatsSubcommand());
}

@Override
public void onDisable() {
    TeamsAPI.unregisterSubcommand(statsSubcommand);
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

### Claim events

Fired by providers that implement `TeamsClaimService`.

| Event | Cancellable | When fired |
|-------|-------------|------------|
| `TeamClaimEvent` | Yes | Before a chunk is claimed |
| `TeamUnclaimEvent` | Yes | Before a chunk is unclaimed |

### Power events

Fired by providers that implement `TeamsPowerService`.

| Event | Cancellable | When fired |
|-------|-------------|------------|
| `TeamPowerGainEvent` | Yes | Before a player's power is increased. Listeners can modify the gain amount. |
| `TeamPowerLossEvent` | Yes | Before a player's power is decreased. Listeners can modify the loss amount. |

### Relation events

Fired by providers that implement `TeamsRelationService`.

| Event | Cancellable | When fired |
|-------|-------------|------------|
| `TeamRelationChangeEvent` | Yes | Before an inter-team relation changes. Listeners can override the incoming relation via `setNewRelation()`. |

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

@EventHandler
public void onClaim(TeamClaimEvent event) {
    // Cancel to block the claim
}

@EventHandler
public void onRelationChange(TeamRelationChangeEvent event) {
    // Override or cancel an incoming relation change
}
```

## Using TeamsAPI from a Velocity plugin

If your plugin runs on a Velocity proxy rather than a Bukkit server, use the
separate `teams-api-velocity` artifact. It exposes the same conceptual API but
returns `CompletableFuture<T>` from every method because answers are delivered
over a plugin messaging channel from a backend Bukkit server.

### 1. Add the dependency

```xml
<dependency>
    <groupId>com.github.ez-plugins</groupId>
    <artifactId>teams-api-velocity</artifactId>
    <version>1.7.0</version>
    <scope>provided</scope>
</dependency>
```

### 2. Query team data

```java
VelocityTeamsService service = VelocityTeamsAPI.getService();
if (service == null) {
    logger.warn("No TeamsAPI backend available.");
    return;
}

service.getPlayerTeam(player.getUniqueId())
    .thenAccept(optional -> optional.ifPresent(team ->
        player.sendMessage(Component.text("Your team: " + team.getName()))));
```

All methods time out after **5 seconds** if the backend does not respond.
See the [Velocity Guide](velocity) for installation and full API details.

For an end-to-end proxy example, see [Consumer Tutorial (Proxy)](consumer-tutorial-proxy).

## API versioning

Check `TeamsAPI.API_VERSION` at runtime if you need to guard against future
breaking changes:

```java
String version = TeamsAPI.API_VERSION; // e.g. "1.7.0"
```

TeamsAPI follows Semantic Versioning. A major version bump signals breaking
changes in `TeamsService` or the model interfaces. Minor bumps add optional,
backward-compatible features. Patch bumps are bug fixes only.

## See also

- [Consumer Tutorial (Bukkit)](consumer-tutorial-bukkit): end-to-end Bukkit consumer plugin
- [Consumer Tutorial (Proxy)](consumer-tutorial-proxy): end-to-end Velocity/Bungee consumer plugin
- [Registering Subcommands](consumer-subcommands): consumer-side custom subcommands
- [Team Provider](provider-teams): implementing `TeamsService` in your team plugin
- [Invite Provider](provider-invites): implementing `TeamsInviteService` for invitation support
- [Warp Provider](provider-warps): implementing `TeamsWarpService` for warp support
- [Chest Provider](provider-chests): implementing `TeamsChestService` for team chest support
- [Velocity Guide](velocity): using `teams-api-velocity` on a Velocity proxy
- [API Reference](api): interface and model overview
- [GitHub repository](https://github.com/ez-plugins/teams-api)
- [Jitpack page](https://jitpack.io/#ez-plugins/teams-api)
