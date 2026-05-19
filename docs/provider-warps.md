---
title: Warp Provider
nav_order: 5
parent: Provider Guide
description: "How to implement and register TeamsWarpService for team warp support"
---

# Warp Provider
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

`TeamsWarpService` is an optional extension for managing named warp points
belonging to teams. It is registered independently of `TeamsService`, and
consumers check `TeamsAPI.isWarpAvailable()` before using it.

## 1. Implement `TeamsWarpService`

```java
public class MyWarpService implements TeamsWarpService {

    @Override
    public boolean setWarp(UUID teamId, String name, Location location, UUID creatorUUID) {
        // Fire TeamWarpSetEvent first.
        // Create or overwrite the named warp.
        // Return false if the event is cancelled or the team does not exist.
    }

    @Override
    public boolean removeWarp(UUID teamId, String name) {
        // Fire TeamWarpDeleteEvent first.
        // Remove the warp record.
        // Return false if the event is cancelled or no such warp exists.
    }

    @Override
    public Optional<TeamWarp> getWarp(UUID teamId, String name) {
        // Return a read-only snapshot, or empty if the warp does not exist.
    }

    @Override
    public Collection<TeamWarp> getWarps(UUID teamId) {
        // Return an unmodifiable collection of all warps for the team.
        // Return an empty collection if the team has no warps or does not exist.
    }
}
```

### Event firing guidelines

| Event | Cancellable | When to fire |
|-------|-------------|--------------|
| `TeamWarpSetEvent` | Yes | Before creating or updating a warp |
| `TeamWarpDeleteEvent` | Yes | Before removing a warp |

Both events are cancellable. If cancelled, do not modify storage and return
`false` from the corresponding method.

## 2. Implement `TeamWarp`

Your `getWarp` and `getWarps` methods return `TeamWarp` objects. This is an
interface defined in `teams-api`; you must supply your own implementation.

```java
public class MyTeamWarp implements TeamWarp {

    private final UUID teamId;
    private final String name;
    private final Location location;
    private final UUID creatorUUID;
    private final Instant createdAt;

    @Override
    public UUID getTeamId() { return teamId; }

    @Override
    public String getName() { return name; }

    @Override
    public Location getLocation() { return location; }

    @Override
    public UUID getCreatorUUID() { return creatorUUID; }

    @Override
    public Instant getCreatedAt() { return createdAt; }
}
```

Return read-only snapshots. Do not expose mutable internal warp state through
this interface.

If your implementation does not track creation time, return `Instant.EPOCH` as
a sentinel value, as documented in the interface contract.

### Warp name uniqueness

Warp names are unique within a team. The `setWarp` method should overwrite an
existing warp if one with the same name already exists for that team, or create
a new one if it does not. Case-sensitivity of warp names is
implementation-defined; document your chosen behaviour clearly for consumers.

## 3. Register and unregister

Register and unregister the warp service alongside the core team service.

```java
private MyTeamsService teamsService;
private MyWarpService warpService;

@Override
public void onEnable() {
    teamsService = new MyTeamsService(this);
    warpService = new MyWarpService(this);
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerWarpProvider(this, warpService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterWarpProvider(warpService);
}
```

To register at a specific priority:

```java
TeamsAPI.registerWarpProvider(this, warpService, ServicePriority.High);
```

## 4. Declare the soft-dependency in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

## See also

- [Team Provider](../provider-teams): implementing the core `TeamsService`
- [Invite Provider](../provider-invites): adding optional invitation support
- [Custom Subcommands](../provider-subcommands): injecting subcommands into `/teamsapi`
- [API Reference](../api): full interface and model documentation
