---
title: Team Provider
nav_order: 2
parent: Provider Guide
description: "How to implement and register TeamsService in your team plugin"
---

# Team Provider
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

This page is for developers building a team plugin that wants to expose its data
through TeamsAPI. Implementing `TeamsService` and registering it with the API
lets any consumer plugin access your team data without importing your classes
directly.

## 1. Add the dependency

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
    <version>1.3.0</version>
    <scope>provided</scope>
</dependency>
```

**Gradle**:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.3.0'
}
```

The `provided`/`compileOnly` scope is correct because `TeamsAPI.jar` on the
server supplies the interface classes at runtime.

## 2. Implement `TeamsService`

Create a class that implements every method in `TeamsService`. The interface
covers team lifecycle, lookup, and membership operations.

```java
public class MyTeamsService implements TeamsService {

    @Override
    public Optional<Team> createTeam(String name, UUID ownerUUID) {
        // Fire TeamCreateEvent before persisting.
        // Return an empty Optional if the event is cancelled or the name is taken.
    }

    @Override
    public boolean deleteTeam(UUID teamId) {
        // Fire TeamDeleteEvent before removing.
        // Return false if no team with that ID exists.
    }

    @Override
    public Optional<Team> getPlayerTeam(UUID playerUUID) {
        // Return the team this player currently belongs to.
    }

    // Implement all remaining interface methods.
}
```

Refer to the [API Reference](../api#teamsservice-interface) for the full list of
methods you must implement.

### Event firing guidelines

Providers are encouraged to fire core events before making state changes.

| Event | Fire before |
|-------|-------------|
| `TeamCreateEvent` | Persisting the new team |
| `TeamDeleteEvent` | Removing the team |
| `TeamJoinEvent` | Adding a member |
| `TeamLeaveEvent` | Removing a member |
| `TeamRoleChangeEvent` | Updating a member's role |

All core events are cancellable. If an event is cancelled, discard the operation
and return the appropriate failure value (`false` or an empty `Optional`).

## 3. Implement `Team` and `TeamMember`

Your `TeamsService` methods return `Team` and `TeamMember` objects. These are
interfaces defined in `teams-api`; you must supply your own implementations.

```java
public class MyTeam implements Team {

    private final UUID id;
    private final String name;
    // ... other fields

    @Override
    public UUID getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public Collection<TeamMember> getMembers() {
        // Return a snapshot, not a live mutable collection.
    }

    // Implement all remaining interface methods.
}
```

Return read-only snapshots. Do not expose mutable internal state through these
interfaces.

## 4. Register and unregister

Register in `onEnable` and unregister in `onDisable`. Failing to unregister can
leave a stale provider in the `ServicesManager`.

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

To register at a specific priority:

```java
TeamsAPI.registerProvider(this, teamsService, ServicePriority.High);
```

When multiple providers are registered, Bukkit's `ServicesManager` selects the
one with the highest priority. `ServicePriority.Normal` is the default.

## 5. Declare the soft-dependency in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

`softdepend` is sufficient because the provider registers itself at startup.
TeamsAPI does not need to be present for your plugin to load; it only needs to
exist before registration is attempted.

## See also

- [Invite Provider](../provider-invites): adding optional invitation support
- [Warp Provider](../provider-warps): adding optional warp support
- [Custom Subcommands](../provider-subcommands): injecting subcommands into `/teamsapi`
- [API Reference](../api): full interface and model documentation
