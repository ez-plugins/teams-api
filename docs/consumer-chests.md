---
title: Consumer Team Chests
nav_order: 3
parent: Consumer Guide
description: "How addon plugins read and modify team chest contents through TeamsChestService"
---

# Consumer Team Chests
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

`TeamsChestService` is optional. Always guard access with
`TeamsAPI.isChestAvailable()` and handle `false` gracefully.

## Read team chest contents

```java
if (!TeamsAPI.isChestAvailable()) {
    player.sendMessage("Team chest support is unavailable.");
    return;
}

final TeamsChestService chests = TeamsAPI.getChestService();
final Collection<String> chestIds = chests.getChestIds(teamId);
final Collection<ItemStack> defaultContents = chests.getContents(teamId);
final Collection<ItemStack> vaultContents = chests.getContents(teamId, "vault");
player.sendMessage("Team chest count: " + chestIds.size());
```

## Set chest contents

```java
if (!TeamsAPI.isChestAvailable()) {
    player.sendMessage("Team chest support is unavailable.");
    return;
}

final TeamsChestService chests = TeamsAPI.getChestService();
final boolean replaced = chests.setContents(teamId, "vault", newContents);
player.sendMessage(replaced ? "Chest contents replaced." : "Could not replace chest contents.");
```

## Add an item

```java
if (!TeamsAPI.isChestAvailable()) {
    player.sendMessage("Team chest support is unavailable.");
    return;
}

final TeamsChestService chests = TeamsAPI.getChestService();
final boolean added = chests.addItem(teamId, "vault", itemStack);
player.sendMessage(added ? "Item added to team chest." : "Could not add item.");
```

## Remove an item

```java
if (!TeamsAPI.isChestAvailable()) {
    player.sendMessage("Team chest support is unavailable.");
    return;
}

final TeamsChestService chests = TeamsAPI.getChestService();
final boolean removed = chests.removeItem(teamId, "vault", itemStack);
player.sendMessage(removed ? "Item removed from team chest." : "Item not found.");
```

## Notes

- `TeamsChestService` methods are provider-defined for slot order, stacking, and
  removal matching behavior.
- Single-chest providers can expose only the default chest id (`default`).
- Treat `getContents` as read-only snapshot data and avoid mutating returned objects.
- Keep chest logic optional so your plugin still works with providers that only
  implement the core `TeamsService`.
