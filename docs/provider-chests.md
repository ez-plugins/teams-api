---
title: Chest Provider
nav_order: 6
parent: Provider Guide
description: "How to implement and register TeamsChestService for team chest support"
---

# Chest Provider
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

`TeamsChestService` is an optional extension for managing team chest contents.
It is registered independently of `TeamsService`, and consumers check
`TeamsAPI.isChestAvailable()` before using it.

## 1. Implement `TeamsChestService`

```java
public class MyChestService implements TeamsChestService {

    @Override
    public Collection<String> getChestIds(UUID teamId) {
        // Return identifiers such as: default, vault, resources.
        // Single-chest providers may return Collections.singleton("default").
    }

    @Override
    public Collection<ItemStack> getContents(UUID teamId) {
        // Return an unmodifiable snapshot of the team's chest contents.
        // This is the default chest.
    }

    @Override
    public Collection<ItemStack> getContents(UUID teamId, String chestId) {
        // Return an unmodifiable snapshot for the requested chest id.
        // Return an empty collection if the chest id does not exist.
    }

    @Override
    public boolean setContents(UUID teamId, Collection<ItemStack> contents) {
        // Replace contents in the default chest.
    }

    @Override
    public boolean setContents(UUID teamId, String chestId, Collection<ItemStack> contents) {
        // Replace contents in the specified chest id.
        // Return false if the chest id does not exist.
    }

    @Override
    public boolean addItem(UUID teamId, ItemStack item) {
        // Add into the default chest.
    }

    @Override
    public boolean addItem(UUID teamId, String chestId, ItemStack item) {
        // Add into the specified chest id.
        // Return false if the chest id does not exist or the item cannot be added.
    }

    @Override
    public boolean removeItem(UUID teamId, ItemStack item) {
        // Remove from the default chest.
    }

    @Override
    public boolean removeItem(UUID teamId, String chestId, ItemStack item) {
        // Remove from the specified chest id.
        // Return false if the item/chest id is not found.
    }
}
```

### Semantics guidelines

- `getContents` should never return `null`; return an empty collection instead.
- `getChestIds` should never return `null`; return an empty collection instead.
- Return read-only snapshots from `getContents` to avoid exposing internal mutable state.
- Slot ordering, stacking behavior, and item matching for remove operations are
  provider-defined; document your concrete behavior for consumer plugins.

## 2. Register and unregister

Register and unregister the chest service alongside the core team service.

```java
private MyTeamsService teamsService;
private MyChestService chestService;

@Override
public void onEnable() {
    teamsService = new MyTeamsService(this);
    chestService = new MyChestService(this);
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerChestProvider(this, chestService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterChestProvider(chestService);
}
```

To register at a specific priority:

```java
TeamsAPI.registerChestProvider(this, chestService, ServicePriority.High);
```

## 3. Declare the soft-dependency in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

## See also

- [Team Provider](../provider-teams): implementing the core `TeamsService`
- [Warp Provider](../provider-warps): adding optional warp support
- [API Reference](../api): full interface and model documentation
