# TeamsAPI

[![CI](https://github.com/ez-plugins/teams-api/actions/workflows/ci.yml/badge.svg)](https://github.com/ez-plugins/teams-api/actions)
[![License](https://img.shields.io/github/license/ez-plugins/teams-api)](LICENSE)
[![Jitpack](https://jitpack.io/v/ez-plugins/teams-api.svg)](https://jitpack.io/#ez-plugins/teams-api)

TeamsAPI is a universal, timeless bridge plugin for Minecraft servers. Inspired by
[Vault](https://github.com/MilkBowl/VaultAPI), it defines a clean, stable interface
for team operations so any plugin that needs team data can work with any compatible
team plugin — without coupling them together.

---

## How It Works

```
Your Plugin (consumer)  →  TeamsAPI (bridge)  →  Team Plugin (provider)
```

- **Providers** (e.g. faction, clan, guild plugins) implement `TeamsService` and
  register with TeamsAPI during `onEnable()`.
- **Consumers** (any plugin that needs team data) call `TeamsAPI.getService()` and
  use the returned `TeamsService` — without knowing which team plugin is installed.
- **Server owners** install `TeamsAPI.jar` alongside any single compatible team plugin.

---

## Download

[GitHub Releases](https://github.com/ez-plugins/teams-api/releases) |
[Modrinth](https://modrinth.com/plugin) | [Hangar](https://hangar.papermc.io/EzPlugins)

---

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

### Consumer usage

```java
// In onEnable() or lazily:
if (!TeamsAPI.isAvailable()) {
    getLogger().warning("No team plugin found — team features disabled.");
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

### Events

All provider events are cancellable and extend `TeamEvent`:

```java
@EventHandler
public void onTeamJoin(TeamJoinEvent event) {
    if (event.getTeam().getSize() >= 10) {
        event.setCancelled(true);
    }
}
```

For the complete API reference, see [docs/api.md](docs/api.md).  
For integration examples, see [docs/developer-guide.md](docs/developer-guide.md).

---

## Compatibility

- **Java**: 21+
- **Server software**: Bukkit, Paper, Spigot, Purpur
- **Plugin API baseline**: 1.21+

---

## Build from Source

```bash
# Compile
mvn -q -DskipTests compile

# Run tests
mvn -q -pl teams-api test

# Build server JAR
mvn -q -DskipTests package
```

Build requirements: Java 21+, Maven 3.8+.

---

## Project Modules

| Module              | Description                                                 |
|---------------------|-------------------------------------------------------------|
| `teams-api/`        | Public API — interfaces, models, and events. Depend on this. |
| `teams-api-plugin/` | Bukkit plugin packaging. Server owners install this JAR.     |

---

## Contributing

1. `mvn -q -DskipTests compile` — must succeed.
2. `mvn -q -pl teams-api test` — all tests must pass.
3. `mvn -q -pl teams-api checkstyle:check` — zero violations.
4. Add tests for non-trivial logic changes.
5. Update Javadoc whenever a public API changes.

See [AGENTS.md](AGENTS.md) for full coding standards.

---

## License

MIT — see [LICENSE](LICENSE).
