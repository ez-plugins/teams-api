# TeamsAPI

[![CI](https://github.com/ez-plugins/teams-api/actions/workflows/ci.yml/badge.svg)](https://github.com/ez-plugins/teams-api/actions)
[![codecov](https://codecov.io/gh/ez-plugins/teams-api/branch/main/graph/badge.svg)](https://codecov.io/gh/ez-plugins/teams-api)
[![License](https://img.shields.io/github/license/ez-plugins/teams-api)](LICENSE)
[![Jitpack](https://jitpack.io/v/ez-plugins/teams-api.svg)](https://jitpack.io/#ez-plugins/teams-api)

TeamsAPI is a universal bridge plugin for Minecraft servers. Inspired by
[Vault](https://github.com/MilkBowl/VaultAPI), it defines a clean, stable interface
for team operations so any plugin that needs team data can work with any compatible
team plugin without coupling them together.

## How It Works

```text
Your Plugin (consumer)  ->  TeamsAPI (bridge)  ->  Team Plugin (provider)
```

- **Providers** (e.g. faction, clan, guild plugins) implement `TeamsService` and
  register with TeamsAPI during `onEnable()`.
- **Consumers** (any plugin that needs team data) call `TeamsAPI.getService()` and
  use the returned `TeamsService` without knowing which team plugin is installed.
- **Server owners** install `TeamsAPI.jar` alongside any single compatible team plugin.

## Download

[GitHub Releases](https://github.com/ez-plugins/teams-api/releases) |
[Modrinth](https://modrinth.com/plugin) | [Hangar](https://hangar.papermc.io/EzPlugins)

## For Developers

### Add the dependency

**Maven** (via [Jitpack](https://jitpack.io/#ez-plugins/teams-api)):

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
    <version>1.6.1</version>
    <scope>provided</scope>
</dependency>
```

**Gradle**:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.6.1'
}
```

### Consumer usage

```java
// In onEnable() or lazily:
if (!TeamsAPI.isAvailable()) {
    getLogger().warning("No team plugin found. Team features disabled.");
    return;
}

// Anywhere team data is needed:
TeamsService teams = TeamsAPI.getService();
Optional<Team> team = teams.getPlayerTeam(player.getUniqueId());
team.ifPresent(t -> player.sendMessage("Team: " + t.getDisplayName()));
```

### Provider registration

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

### Invite service (optional)

If the active team plugin supports invitations, a `TeamsInviteService` is available:

```java
if (TeamsAPI.isInviteAvailable()) {
    TeamsInviteService invites = TeamsAPI.getInviteService();
    invites.invitePlayer(teamId, sender.getUniqueId(), target.getUniqueId());
}
```

Providers that support invitations register the service alongside `TeamsService`:

```java
@Override
public void onEnable() {
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerInviteProvider(this, inviteService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterInviteProvider(inviteService);
}
```

### Warp service (optional)

If the active team plugin supports named warps, a `TeamsWarpService` is available:

```java
if (TeamsAPI.isWarpAvailable()) {
    TeamsWarpService warps = TeamsAPI.getWarpService();
    warps.getWarp(teamId, "home").ifPresent(w -> player.teleport(w.getLocation()));
}
```

Providers that support warps register the service alongside `TeamsService`:

```java
@Override
public void onEnable() {
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerWarpProvider(this, warpService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterWarpProvider(warpService);
}
```

### Claim service (optional)

If the active team plugin supports chunk claims, a `TeamsClaimService` is available:

```java
if (TeamsAPI.isClaimAvailable()) {
    TeamsClaimService claims = TeamsAPI.getClaimService();
    Chunk chunk = player.getLocation().getChunk();
    boolean claimed = claims.claimChunk(
        teamId, player.getUniqueId(),
        chunk.getWorld().getName(), chunk.getX(), chunk.getZ()
    );
}
```

Providers that support claims register the service alongside `TeamsService`:

```java
@Override
public void onEnable() {
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerClaimProvider(this, claimService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterClaimProvider(claimService);
}
```

### Power service (optional)

If the active team plugin exposes power values, a `TeamsPowerService` is available:

```java
if (TeamsAPI.isPowerAvailable()) {
    TeamsPowerService power = TeamsAPI.getPowerService();
    double current = power.getTeamPower(teamId);
    double max = power.getTeamMaxPower(teamId);
    player.sendMessage("Team power: " + current + " / " + max);
}
```

Providers that expose power register the service alongside `TeamsService`:

```java
@Override
public void onEnable() {
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerPowerProvider(this, powerService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterPowerProvider(powerService);
}
```

### Relation service (optional)

If the active team plugin supports inter-team diplomacy, a `TeamsRelationService` is
available. Relations are directional — team A can declare `ALLY` toward team B
independently of what team B declares toward team A.

```java
if (TeamsAPI.isRelationAvailable()) {
    TeamsRelationService relations = TeamsAPI.getRelationService();

    // Declare an alliance
    relations.setRelation(myTeamId, theirTeamId, TeamRelation.ALLY, player.getUniqueId());

    // Query the current relation
    TeamRelation rel = relations.getRelation(myTeamId, theirTeamId);
    player.sendMessage("Relation: " + rel.name());

    // Convenience helpers (mutual check)
    if (relations.areAllies(myTeamId, theirTeamId)) {
        player.sendMessage("You are mutual allies!");
    }
}
```

Providers that support relations register the service alongside `TeamsService`:

```java
@Override
public void onEnable() {
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerRelationProvider(this, relationService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterRelationProvider(relationService);
}
```

`TeamRelation` values (lowest → highest hostility): `ALLY`, `TRUCE`, `NEUTRAL`, `ENEMY`.

Any plugin can register a `TeamsSubcommand` via `TeamsAPI.registerSubcommand()`. Team
plugins call `TeamsAPI.getSubcommands()` in their own command executor to dispatch them,
allowing third-party plugins to extend the team plugin's command tree without any direct
coupling between plugins.

Extend `AbstractTeamsSubcommand` (recommended) or implement `TeamsSubcommand` directly:

```java
public class StatsSubcommand extends AbstractTeamsSubcommand {
    public StatsSubcommand() {
        super("stats", "Show faction statistics.", "myfactions.stats");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // handle the command
        return true; // return false to trigger the usage hint
    }
}

// In onEnable:
TeamsAPI.registerSubcommand(this, new StatsSubcommand());

// In onDisable:
TeamsAPI.unregisterSubcommand(statsSubcommand);
```

Team plugins dispatch registered subcommands inside their own command executor:

```java
for (TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
    if (sub.getName().equalsIgnoreCase(args[0])) {
        String perm = sub.getPermission();
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage("You do not have permission.");
            return true;
        }
        if (!sub.execute(sender, args)) {
            sender.sendMessage("Usage: " + sub.getUsage());
        }
        return true;
    }
}
```

| Method | Returns | Description |
|--------|---------|-------------|
| `getName()` | `String` | Matched case-insensitively against `args[0]` |
| `getDescription()` | `String` | Optional description for help output |
| `getPermission()` | `String` | Required permission, or `null` for no check |
| `execute(sender, args)` | `boolean` | Called when dispatched; return `false` to show usage |
| `getUsage()` | `String` | Usage hint sent when `execute` returns `false` |
| `tabComplete(sender, args)` | `List<String>` | Tab-completion suggestions; default: empty list |

### Events

Provider events extend `TeamEvent`. Core events are cancellable:

```java
@EventHandler
public void onTeamJoin(TeamJoinEvent event) {
    if (event.getTeam().getSize() >= 10) {
        event.setCancelled(true);
    }
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
public void onUnclaim(TeamUnclaimEvent event) {
    // Cancel to block the unclaim
}
```

For the complete API reference, see [docs/api.md](docs/api.md).
For integration examples, see [docs/developer-guide.md](docs/developer-guide.md).

## Proxy Support

TeamsAPI includes bridge plugins for both major proxy platforms:

| Proxy | Plugin | Guide |
|-------|--------|-------|
| Velocity | `teams-api-velocity` | [Velocity Guide](docs/velocity.md) |
| BungeeCord / Waterfall | `teams-api-bungeecord` | [BungeeCord Guide](docs/bungeecord.md) |

Both bridges expose an async API (`CompletableFuture<T>`) so proxy-side plugins can
query team data from backend servers without direct coupling.

### Multi-proxy (Redis)

Both bridge plugins support **multi-proxy networks** via Redis Pub/Sub. When Redis is
enabled in `config.yml`, queries that cannot be fulfilled by a local player are
automatically forwarded to another proxy in the network. All proxies must share the
same Redis instance.

```yaml
# plugins/teamsapi/config.yml
redis:
  enabled: true
  host: "your-redis-host"
  port: 6379
  prefix: "teamsapi:"
```

See the [Velocity Guide](docs/velocity.md) or [BungeeCord Guide](docs/bungeecord.md)
for the full configuration reference.

## Compatibility

| Requirement | Version |
|-------------|---------|
| Java | 17+ (25 recommended) |
| Server software | Paper / Spigot / Purpur / Folia 1.16+ |
| Build tool | Maven 3.8+ or Gradle 8+ |

## Build from Source

```bash
# Compile
mvn -q -DskipTests compile

# Run tests
mvn -q -pl teams-api test

# Build server JAR
mvn -q -DskipTests package
```

## Project Modules

| Module | Description |
|--------|-------------|
| `teams-api/` | Public API: interfaces, models, and events. Depend on this. |
| `teams-api-plugin/` | Bukkit plugin packaging. Server owners install this JAR. |
| `teams-api-velocity/` | Velocity proxy plugin. Bridges team queries to backend servers. Supports Redis for multi-proxy setups. |
| `teams-api-bungeecord/` | BungeeCord / Waterfall proxy plugin. Mirrors the Velocity bridge. Supports Redis for multi-proxy setups. |

## Contributing

1. `mvn -q -DskipTests compile` must succeed.
2. `mvn -q -pl teams-api test` all tests must pass.
3. `mvn -q -pl teams-api checkstyle:check` zero violations.
4. Add tests for non-trivial logic changes.
5. Update Javadoc whenever a public API changes.

See [AGENTS.md](AGENTS.md) for full coding standards.

## License

MIT — see [LICENSE](LICENSE).
