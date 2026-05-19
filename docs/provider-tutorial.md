---
title: Provider Tutorial (Step-by-step)
nav_order: 1
parent: Provider Guide
description: "Simple step-by-step tutorial for implementing TeamsAPI in a team or faction plugin"
---

# Provider Tutorial (Step-by-step)
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

This tutorial is for team/faction plugin developers who want to expose their
plugin through TeamsAPI so addon plugins can read team data.

## Goal

By the end, your plugin will:

- implement `TeamsService`,
- register itself with TeamsAPI on enable,
- unregister cleanly on disable,
- be safely consumable by third-party addon plugins.

## Step 1: Add the API dependency

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
    <version>1.7.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.7.0'
}
```

Use `provided`/`compileOnly` so you do not shade TeamsAPI into your JAR.

## Step 2: Add `softdepend` in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

This ensures your plugin still loads even if TeamsAPI is missing.

## Step 3: Create a `TeamsService` implementation

Create a class that maps your internal team system to the TeamsAPI interface.

```java
public final class MyTeamsService implements TeamsService {

    private final MyFactionPlugin plugin;

    public MyTeamsService(final MyFactionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<Team> getTeamById(final UUID teamId) {
        final InternalFaction faction = plugin.getFactionManager().findById(teamId);
        if (faction == null) {
            return Optional.empty();
        }
        return Optional.of(new MyTeamAdapter(faction));
    }

    @Override
    public Optional<Team> getPlayerTeam(final UUID playerUUID) {
        final InternalFaction faction = plugin.getFactionManager().findByMember(playerUUID);
        if (faction == null) {
            return Optional.empty();
        }
        return Optional.of(new MyTeamAdapter(faction));
    }
}
```

Important:

- Return `Optional.empty()` when data is not found.
- Do not return `null` from service methods that return `Optional<T>`.
- Keep your own implementation classes inside your plugin, not in `teams-api`.

## Step 4: Adapt your team model to `Team` / `TeamMember`

Implement adapters for TeamsAPI model interfaces.

```java
public final class MyTeamAdapter implements Team {

    private final InternalFaction faction;

    public MyTeamAdapter(final InternalFaction faction) {
        this.faction = faction;
    }

    @Override
    public UUID getId() {
        return faction.getId();
    }

    @Override
    public String getName() {
        return faction.getName();
    }

    @Override
    public String getDisplayName() {
        return faction.getDisplayName();
    }

    @Override
    public int getSize() {
        return faction.getMembers().size();
    }

    @Override
    public Set<TeamMember> getMembers() {
        return faction.getMembers().stream()
            .map(MyTeamMemberAdapter::new)
            .collect(Collectors.toSet());
    }
}
```

## Step 5: Register provider in `onEnable`

```java
public final class MyFactionPlugin extends JavaPlugin {

    private MyTeamsService teamsService;

    @Override
    public void onEnable() {
        teamsService = new MyTeamsService(this);
        TeamsAPI.registerProvider(this, teamsService);
        getLogger().info("Registered TeamsService with TeamsAPI.");
    }
}
```

## Step 6: Unregister provider in `onDisable`

```java
@Override
public void onDisable() {
    if (teamsService != null) {
        TeamsAPI.unregisterProvider(teamsService);
    }
}
```

## Step 7: Verify on a test server

1. Install `teams-api-plugin` and your team/faction plugin.
2. Start the server.
3. Check logs for your provider registration message.
4. Install a consumer addon plugin and confirm it can read team data.

## Step 8: Add optional services later

After core `TeamsService` works, you can add optional providers:

- `TeamsInviteService`
- `TeamsWarpService`
- `TeamsClaimService`
- `TeamsPowerService`
- `TeamsRelationService`
- `TeamsNotificationService`

Each optional service is registered independently.

## Common mistakes

- Returning `null` instead of `Optional.empty()`.
- Forgetting to unregister on disable.
- Shading `teams-api` into your plugin JAR.
- Exposing provider implementation classes as public API for other plugins.

## Optional integration and shading

If you want TeamsAPI support to be optional and avoid runtime conflicts caused
by incorrect shading, follow:

- [Optional Integration & Shading](provider-optional-integration)

## Next steps

- [Team Provider](provider-teams): full provider reference and patterns
- [Custom Subcommands](provider-subcommands): let addons extend your command tree
- [API Reference](api): full service and model method tables
