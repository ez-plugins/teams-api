---
title: Invite Provider
nav_order: 2
parent: Developer Guide
description: "How to implement and register TeamsInviteService for invitation support"
---

# Invite Provider
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

`TeamsInviteService` is an optional extension for team invitation flows. It is
registered independently of `TeamsService`, and consumers check
`TeamsAPI.isInviteAvailable()` before using it. You can implement it alongside
`TeamsService` or ship it in a separate plugin.

## 1. Implement `TeamsInviteService`

```java
public class MyInviteService implements TeamsInviteService {

    @Override
    public boolean invitePlayer(UUID teamId, UUID inviterUUID, UUID inviteeUUID) {
        // Fire TeamInviteEvent first.
        // Return false if the event is cancelled or a pending invite already exists.
    }

    @Override
    public Optional<Team> acceptInvite(UUID teamId, UUID playerUUID) {
        // Add the player to the team as MEMBER.
        // Fire TeamInviteAcceptEvent after a successful join.
        // Return an empty Optional if no invite exists or the join failed.
    }

    @Override
    public boolean declineInvite(UUID teamId, UUID playerUUID) {
        // Remove the pending invite record.
        // Fire TeamInviteDeclineEvent after removal.
        // Return false if no invite existed.
    }
}
```

### Event firing guidelines

| Event | Cancellable | When to fire |
|-------|-------------|--------------|
| `TeamInviteEvent` | Yes | Before recording the invite |
| `TeamInviteAcceptEvent` | No | After the player has joined the team |
| `TeamInviteDeclineEvent` | No | After the invite record is removed |

`TeamInviteEvent` is cancellable. If cancelled, do not record the invite and
return `false` from `invitePlayer`.

`TeamInviteAcceptEvent` and `TeamInviteDeclineEvent` are informational. Fire
them after the state change has already been committed.

## 2. Register and unregister

Register and unregister the invite service alongside the core team service.

```java
private MyTeamsService teamsService;
private MyInviteService inviteService;

@Override
public void onEnable() {
    teamsService = new MyTeamsService(this);
    inviteService = new MyInviteService(this);
    TeamsAPI.registerProvider(this, teamsService);
    TeamsAPI.registerInviteProvider(this, inviteService);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterProvider(teamsService);
    TeamsAPI.unregisterInviteProvider(inviteService);
}
```

To register at a specific priority:

```java
TeamsAPI.registerInviteProvider(this, inviteService, ServicePriority.High);
```

## 3. Declare the soft-dependency in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

## See also

- [Team Provider](../provider-teams): implementing the core `TeamsService`
- [Warp Provider](../provider-warps): adding optional warp support
- [Custom Subcommands](../provider-subcommands): injecting subcommands into `/teamsapi`
- [API Reference](../api): full interface and model documentation
