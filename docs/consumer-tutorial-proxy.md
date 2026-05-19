---
title: Consumer Tutorial (Proxy)
nav_order: 3
parent: Consumer Guide
description: "Step-by-step tutorial to build a Velocity/Bungee consumer plugin using TeamsAPI bridge artifacts"
---

# Consumer Tutorial (Proxy)
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

This tutorial shows how to consume TeamsAPI from a proxy plugin where all calls
are asynchronous.

## Choose your platform

- Velocity: use `teams-api-velocity` and `VelocityTeamsAPI`.
- BungeeCord/Waterfall: use `teams-api-bungeecord` and `BungeeTeamsAPI`.

The backend Bukkit servers must still run `teams-api-plugin` and a team provider.

## 1. Add the bridge dependency

### Velocity (Maven)

```xml
<dependency>
    <groupId>com.github.ez-plugins</groupId>
    <artifactId>teams-api-velocity</artifactId>
    <version>1.7.0</version>
    <scope>provided</scope>
</dependency>
```

### BungeeCord (Maven)

```xml
<dependency>
    <groupId>com.github.ez-plugins</groupId>
    <artifactId>teams-api-bungeecord</artifactId>
    <version>1.7.0</version>
    <scope>provided</scope>
</dependency>
```

## 2. Declare soft dependency

- Velocity: add optional dependency entry in `velocity-plugin.json`.
- BungeeCord: add `softDepends: [ "TeamsAPI" ]` in `bungee.yml`.

Use optional/soft dependency because bridge presence can vary per network.

## 3. Query team data asynchronously (Velocity)

```java
final VelocityTeamsService service = VelocityTeamsAPI.getService();
if (service == null) {
    logger.warn("TeamsAPI bridge unavailable.");
    return;
}

service.getPlayerTeam(player.getUniqueId())
    .thenAccept(optionalTeam -> {
        if (optionalTeam.isEmpty()) {
            player.sendMessage(Component.text("You are not in a team."));
            return;
        }
        player.sendMessage(Component.text("Team: " + optionalTeam.get().getDisplayName()));
    })
    .exceptionally(throwable -> {
        logger.warn("Could not load team data", throwable);
        return null;
    });
```

## 4. Query team data asynchronously (BungeeCord)

```java
final BungeeTeamsService service = BungeeTeamsAPI.getService();
if (service == null) {
    getLogger().warning("TeamsAPI bridge unavailable.");
    return;
}

service.getPlayerTeam(playerUuid).thenAccept(optionalTeam -> {
    if (optionalTeam.isEmpty()) {
        // send not-in-team message
        return;
    }
    // use optionalTeam.get()
});
```

## 5. Defensive proxy patterns

- Always handle `null` service lookups.
- Always attach `exceptionally(...)` or equivalent failure handling.
- Keep request handlers short; avoid blocking waits (`join()`/`get()`).
- Expect timeouts when no backend answers bridge requests.

## Next steps

- [Velocity Guide](velocity): full setup, config, and troubleshooting
- [BungeeCord Guide](bungeecord): full setup and API behavior
- [Developer Guide](developer-guide): Bukkit-side consumer patterns
