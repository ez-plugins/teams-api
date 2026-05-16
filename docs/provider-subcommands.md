---
title: Custom Subcommands
nav_order: 4
parent: Developer Guide
description: "How to inject custom subcommands into /teamsapi using TeamsSubcommand"
---

# Custom Subcommands
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

`TeamsSubcommand` lets any provider plugin inject additional subcommands into the
`/teamsapi` command tree without shipping a separate Bukkit command. Each registered
subcommand appears in `/teamsapi help` and is dispatched automatically by the
TeamsAPI plugin when a player runs `/teamsapi <name>`.

This is useful when a provider wants to expose commands that players already know.
For example, a Factions plugin that normally uses `/f` can register `f` as a
subcommand so players run `/teamsapi f [args...]` — one familiar short alias,
no extra top-level command registration required.

## 1. Implement `TeamsSubcommand`

### Option A — extend `AbstractTeamsSubcommand` (recommended)

`AbstractTeamsSubcommand` handles the boilerplate for you. Pass name, description,
and (optionally) permission to the constructor; override only what you need.

```java
import com.skyblockexp.teamsapi.api.AbstractTeamsSubcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionsSubcommand extends AbstractTeamsSubcommand {

    public FactionsSubcommand() {
        super("f", "Factions commands (alias: /f).", "myfactions.use");
    }

    @Override
    public String getUsage() {
        return "/teamsapi f <help|stats|top> [args...]";
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            return false; // TeamsAPI prints getUsage()
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        return handleSubcommand((Player) sender, args);
    }

    private boolean handleSubcommand(final Player player, final String[] args) {
        player.sendMessage("Factions: " + args[1]);
        return true;
    }
}
```

`getName()`, `getDescription()`, and `getPermission()` are already implemented.
`getUsage()` and `tabComplete()` have sensible defaults — override either as needed.

### Option B — implement `TeamsSubcommand` directly

Use this when you need full control (e.g. dynamic name resolution or a
permission that changes at runtime).

```java
import com.skyblockexp.teamsapi.api.TeamsSubcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionsSubcommand implements TeamsSubcommand {

    @Override
    public String getName() { return "f"; }

    @Override
    public String getDescription() { return "Factions commands (alias: /f)."; }

    @Override
    public String getPermission() { return "myfactions.use"; }

    @Override
    public String getUsage() { return "/teamsapi f <help|stats|top> [args...]"; }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            return false; // TeamsAPI prints getUsage()
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        sender.sendMessage("Factions: " + args[1]);
        return true;
    }
}
```

### Interface contract

| Method | Returns | Contract |
|--------|---------|----------|
| `getName()` | `String` | Case-insensitive match against `args[0]`. Must be stable across reloads. Use your plugin's familiar short name (e.g. `"f"` for a Factions plugin). |
| `getDescription()` | `String` | Short, single-line description for help output. |
| `getPermission()` | `String` | Permission node, or `null` if anyone with `teamsapi.use` may run it. |
| `getUsage()` | `String` | Usage string sent to the sender when `execute()` returns `false`. Default: `"/teamsapi <name>"`. Override to include expected arguments. |
| `execute(sender, args)` | `boolean` | Return `true` if the command was handled (even on error). Return `false` to let TeamsAPI print the `getUsage()` hint. `args[0]` is always the subcommand name. |
| `tabComplete(sender, args)` | `List<String>` | Tab-completion suggestions. Default returns an empty list. |

### Permission behaviour

If `getPermission()` returns a non-null value, TeamsAPI checks
`sender.hasPermission(permission)` before calling `execute`. A sender without
the permission receives a generic denial message and the subcommand is not
dispatched. The subcommand is also hidden from `/teamsapi help` for that sender.

If `getPermission()` returns `null`, the subcommand is accessible to any sender
who has `teamsapi.use` (the base permission, default `true`).

## 2. Register and unregister

Register in `onEnable`, unregister in `onDisable`. Bukkit's ServicesManager
also unregisters all services for a plugin automatically when it unloads, but
explicit cleanup is best practice.

```java
private final FactionsSubcommand factionsSubcommand = new FactionsSubcommand();

@Override
public void onEnable() {
    TeamsAPI.registerSubcommand(this, factionsSubcommand);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterSubcommand(factionsSubcommand);
}
```

Players can then run `/teamsapi f <subcommand>` and the call is routed to your
implementation.

`registerSubcommand` uses `ServicePriority.Normal`. If two plugins register a
subcommand with the same name, both are registered; TeamsAPI dispatches to the
first match it finds when iterating `getSubcommands()` (highest-priority first
by ServicesManager ordering). Prefer unique names to avoid conflicts.

### Registering multiple subcommands

```java
private final FactionsSubcommand factionsSubcommand = new FactionsSubcommand();
private final ClansSubcommand clansSubcommand = new ClansSubcommand();

@Override
public void onEnable() {
    TeamsAPI.registerSubcommand(this, factionsSubcommand); // /teamsapi f
    TeamsAPI.registerSubcommand(this, clansSubcommand);   // /teamsapi clans
}

@Override
public void onDisable() {
    TeamsAPI.unregisterSubcommand(factionsSubcommand);
    TeamsAPI.unregisterSubcommand(clansSubcommand);
}
```

## 3. Declare the soft-dependency in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

Use `softdepend` (not `depend`) so your plugin can still load on servers that
do not have TeamsAPI installed. Guard the registration call with a null check or
a `isAvailable` guard if TeamsAPI is optional:

```java
@Override
public void onEnable() {
    if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
        TeamsAPI.registerSubcommand(this, factionsSubcommand);
    }
}
```

## 4. Full working example

A Factions plugin that registers `/teamsapi f` so players can type `/teamsapi f
<subcommand>` using the same short alias they already know:

```java
import com.skyblockexp.teamsapi.api.AbstractTeamsSubcommand;
import com.skyblockexp.teamsapi.api.TeamsAPI;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyFactionsPlugin extends JavaPlugin {

    private final AbstractTeamsSubcommand fSubcommand =
        new AbstractTeamsSubcommand("f", "Factions commands.", "myfactions.use") {

            @Override
            public String getUsage() {
                return "/teamsapi f <help|stats|top> [args...]";
            }

            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                if (args.length < 2) {
                    return false; // TeamsAPI prints getUsage()
                }
                // Dispatch to your own subcommand handler.
                return MyFactionsPlugin.this.dispatch((Player) sender, args);
            }

            @Override
            public java.util.List<String> tabComplete(
                    final CommandSender sender, final String[] args) {
                if (args.length == 2) {
                    return java.util.List.of("help", "stats", "top");
                }
                return java.util.Collections.emptyList();
            }
        };

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
            TeamsAPI.registerSubcommand(this, fSubcommand);
        }
    }

    @Override
    public void onDisable() {
        TeamsAPI.unregisterSubcommand(fSubcommand);
    }

    private boolean dispatch(final Player player, final String[] args) {
        player.sendMessage("Factions " + args[1] + ": ...");
        return true;
    }
}
```

## See also

- [Team Provider](provider-teams): implementing the core `TeamsService`
- [Invite Provider](provider-invites): adding optional invitation support
- [Warp Provider](provider-warps): adding optional warp support
- [API Reference](api): full interface and model documentation
