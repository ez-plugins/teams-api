---
title: Working with Roles
nav_order: 4
parent: Consumer Guide
description: "How to read, compare, and display team roles as a consumer plugin"
---

# Working with Roles
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

`TeamRole` is an enum that represents the authority level of a team member.
Every `TeamMember` carries exactly one role. This page covers how to read,
compare, and display roles from a consumer plugin.

## Role constants

| Constant | Priority | Default prefix | Description |
|----------|----------|----------------|-------------|
| `OWNER`  | 100      | `Owner`        | Sole owner of the team. Full authority. |
| `ADMIN`  | 50       | `Admin`        | Can manage regular members. |
| `MEMBER` | 10       | `Member`       | Regular member with no elevated permissions. |

Higher priority means higher rank. A role with priority 100 outranks one with
priority 50.

## Reading a member's role

```java
TeamsService teams = TeamsAPI.getService();
if (teams == null) {
    return;
}

Optional<TeamMember> member = teams.getMemberInfo(teamId, playerUUID);
member.ifPresent(m -> {
    TeamRole role = m.getRole();
    player.sendMessage("Your role: " + role.getPrefix());
});
```

Alternatively, use the role-specific lookup:

```java
Optional<TeamRole> role = teams.getMemberRole(teamId, playerUUID);
role.ifPresent(r -> player.sendMessage("Role: " + r.getPrefix()));
```

## Comparing roles

Use `outranks()` to compare two roles rather than relying on name ordering
or `ordinal()`:

```java
if (actorRole.outranks(targetRole)) {
    // actor can affect the target
}
```

`canManage()` encodes the same check and reads more clearly in permission gates:

```java
TeamRole actorRole  = teams.getMemberRole(teamId, actorUUID).orElse(null);
TeamRole targetRole = teams.getMemberRole(teamId, targetUUID).orElse(null);

if (actorRole == null || targetRole == null) {
    sender.sendMessage("Player is not in this team.");
    return;
}

if (!actorRole.canManage(targetRole)) {
    sender.sendMessage("You cannot manage a member with the same or higher rank.");
    return;
}

// proceed with the action
```

## Displaying prefixes

`getPrefix()` returns the display prefix for a role. By default the values are
`"Owner"`, `"Admin"`, and `"Member"`:

```java
String tag = "[" + role.getPrefix() + "] " + playerName;
player.sendMessage(tag);
```

## Customising prefixes (optional)

Servers or plugins that want custom labels - translations, brand names, or
server lore - can override a role's prefix JVM-wide. Because `TeamRole`
constants are singletons, the override is visible everywhere on the server.
Set it once during `onEnable()`:

```java
@Override
public void onEnable() {
    TeamRole.OWNER.setPrefixOverride("Leader");
    TeamRole.ADMIN.setPrefixOverride("Officer");
    TeamRole.MEMBER.setPrefixOverride("Recruit");
}
```

`getPrefix()` will now return the override everywhere `TeamRole` constants
are used. To remove an override and restore the built-in default, pass `null`:

```java
TeamRole.OWNER.setPrefixOverride(null);  // restores "Owner"
```

To read the built-in default regardless of any active override, use
`getDefaultPrefix()`:

```java
// Always "Owner", "Admin", or "Member" - never an override
String builtIn = TeamRole.OWNER.getDefaultPrefix();
```

### Example: loading prefixes from `config.yml`

```java
@Override
public void onEnable() {
    saveDefaultConfig();

    for (TeamRole role : TeamRole.values()) {
        String key = "role-prefixes." + role.name().toLowerCase(Locale.ROOT);
        if (getConfig().contains(key)) {
            role.setPrefixOverride(getConfig().getString(key));
        }
    }
}
```

And the matching `config.yml` section:

```yaml
role-prefixes:
  owner: "Leader"
  admin: "Officer"
  member: "Recruit"
```

### Bulk prefix management

For setting or clearing multiple overrides at once, use the static helpers:

```java
// Apply a map of overrides (null value clears that role's override)
TeamRole.applyPrefixes(Map.of(
    TeamRole.OWNER,  "[Lord]",
    TeamRole.ADMIN,  "[Officer]",
    TeamRole.MEMBER, "[Recruit]"
));

// Clear all built-in role overrides in one call
TeamRole.resetAllPrefixes();
```

## Custom role definitions

Some team plugins model roles beyond `OWNER`, `ADMIN`, and `MEMBER` (for example
a "Co-Owner" rank). Providers that do this register their custom roles in the
TeamsAPI server-wide registry:

```java
// In the provider plugin's onEnable()
TeamRoleDefinition coOwner = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
TeamsAPI.registerCustomRole(this, coOwner);
```

As a consumer you can look these up at runtime:

```java
// Get a specific custom role by key
Optional<TeamRoleDefinition> role = TeamsAPI.getCustomRole("co_owner");
role.ifPresent(def -> getLogger().info("Priority: " + def.getPriority()));

// Iterate all custom roles, sorted highest priority first
for (TeamRoleDefinition def : TeamsAPI.getCustomRoles()) {
    getLogger().info(def.getKey() + " (" + def.getPrefix() + ")");
}

// Check presence
if (TeamsAPI.isCustomRoleRegistered("co_owner")) {
    // provider has a Co-Owner rank
}
```

The `TeamMember` interface exposes a default `getRoleDefinition()` method that
returns a `TeamRoleDefinition` wrapping the member's current built-in role.
Providers that support custom roles override this to return the precise definition:

```java
TeamMember member = ...;
TeamRoleDefinition def = member.getRoleDefinition();
String display = "[" + def.getPrefix() + "] " + playerName;
```

`TeamRoleDefinition` supports the same prefix-override API as `TeamRole`:

```java
def.setPrefixOverride("[CO]");  // override
def.setPrefixOverride(null);    // restore default
def.getDefaultPrefix();         // always the original value
```

## Displaying role priority

The numeric priority is available via `getPriority()`. It is useful when you
need to sort members by rank or display a numeric level:

```java
int rank = role.getPriority();  // 100, 50, or 10
```

## Common patterns

**Check whether a player is the owner:**

```java
boolean isOwner = team.isOwner(player.getUniqueId());
```

**Check minimum rank:**

```java
private boolean hasMinRole(final Team team, final UUID playerUUID, final TeamRole minimum) {
    return team.getMember(playerUUID)
        .map(m -> m.getRole() == minimum || m.getRole().outranks(minimum))
        .orElse(false);
}
```

**Format member list with prefixes:**

```java
for (TeamMember member : team.getMembers()) {
    String line = "[" + member.getRole().getPrefix() + "] "
        + Bukkit.getOfflinePlayer(member.getPlayerUUID()).getName();
    player.sendMessage(line);
}
```
