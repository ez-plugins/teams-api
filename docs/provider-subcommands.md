---
title: Custom Subcommands
nav_order: 4
parent: Developer Guide
description: "How to dispatch TeamsSubcommand registrations inside your team plugin's own command handler"
---

# Custom Subcommands
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

The custom subcommand system lets any third-party plugin extend your team plugin's
command tree without you knowing about it at compile time. Your team plugin adds a
dispatch call inside its own `CommandExecutor` that checks `TeamsAPI.getSubcommands()`
after handling its own built-in subcommands. When a match is found the registered
`TeamsSubcommand` is called directly — in your command, under your permission system.

This is the same Vault-style decoupling that makes `TeamsService` work: your plugin
dispatches, other plugins register. Neither needs to know about the other.

See [Registering Subcommands](consumer-subcommands) for the consumer side — how
other plugins implement and register a `TeamsSubcommand` that your command will
dispatch.

## 1. Declare the soft-dependency

```yaml
# plugin.yml
softdepend:
  - TeamsAPI
```

Use `softdepend` so your plugin loads on servers without TeamsAPI. The dispatch
loop is a no-op when `getSubcommands()` returns an empty collection, but you still
need to guard the `TeamsAPI` class reference itself (see
[section 4](#4-guard-when-teamsapi-is-absent)).

## 2. Add the dispatch call to your `CommandExecutor`

After handling your plugin's own subcommands, call
`TeamsAPI.dispatchSubcommand(sender, args)`. It returns `true` if a registered
subcommand matched (handled or denied), so you only need one line:

```java
import com.skyblockexp.teamsapi.api.TeamsAPI;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FactionsCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
            final String label, final String[] args) {
        if (args.length == 0) {
            showHelp(sender, label);
            return true;
        }

        // Handle your own built-in subcommands first
        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreate(sender, args);
            case "disband":
                return handleDisband(sender, args);
            // ...
            default:
                break;
        }

        // Fall through to any registered TeamsSubcommand
        if (TeamsAPI.dispatchSubcommand(sender, args)) {
            return true;
        }

        sender.sendMessage("Unknown subcommand. Use /" + label + " help.");
        return true;
    }
}
```

`dispatchSubcommand` checks permissions, calls `execute()`, and sends the usage hint
if `execute()` returns `false` — all in one call.

## 3. Add tab-complete dispatch

Merge your own subcommand names with those from
`TeamsAPI.tabCompleteSubcommands(sender, args)`, which handles prefix filtering
and permission gating automatically:

```java
import com.skyblockexp.teamsapi.api.TeamsAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class FactionsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command,
            final String label, final String[] args) {
        if (args.length == 1) {
            // Merge your own names with registered TeamsSubcommand names
            final List<String> suggestions = new ArrayList<>(List.of("create", "disband"));
            suggestions.addAll(TeamsAPI.tabCompleteSubcommands(sender, args));
            final String prefix = args[0].toLowerCase();
            suggestions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
            return suggestions;
        }
        if (args.length > 1) {
            return TeamsAPI.tabCompleteSubcommands(sender, args);
        }
        return Collections.emptyList();
    }
}
```

## 4. Guard when TeamsAPI is absent

If TeamsAPI is a soft-dependency, wrap the dispatch loop with a null check so the
`TeamsAPI` class reference is only evaluated when the plugin is actually loaded:

```java
if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
    for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
        // ...
    }
}
```

`TeamsAPI.getSubcommands()` returns an empty collection when no subcommands are
registered, so the loop itself is safe. The guard is only needed to prevent a
`ClassNotFoundException` on servers where `TeamsAPI.jar` is not present at all.

## 5. Display registered subcommands in your help output

You can include registered subcommands in your `/factions help` listing:

```java
private void showHelp(final CommandSender sender, final String label) {
    sender.sendMessage("=== /" + label + " help ===");
    sender.sendMessage("/" + label + " create <name>  —  Create a faction");
    sender.sendMessage("/" + label + " disband  —  Disband your faction");

    // Third-party subcommands registered via TeamsAPI
    for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
        final String perm = sub.getPermission();
        if (perm == null || sender.hasPermission(perm)) {
            sender.sendMessage("/" + label + " " + sub.getName()
                    + "  —  " + sub.getDescription());
        }
    }
}
```

## 6. `TeamsSubcommand` interface contract

Third-party plugins that want to extend your command tree implement
`TeamsSubcommand` (or extend `AbstractTeamsSubcommand`) and call
`TeamsAPI.registerSubcommand()`. Your plugin never needs to import or reference
those plugins.

| Method | Returns | Contract |
|--------|---------|---------|
| `getName()` | `String` | Matched case-insensitively against `args[0]`. |
| `getDescription()` | `String` | Short description; use it in your help output if you wish. |
| `getPermission()` | `String` | Permission node to check before dispatching, or `null` for no check. |
| `execute(sender, args)` | `boolean` | Return `true` if handled (including on error). Return `false` to trigger the usage hint. `args[0]` is the subcommand name. |
| `getUsage()` | `String` | Usage string sent to the sender when `execute()` returns `false`. |
| `tabComplete(sender, args)` | `List<String>` | Completion suggestions; default returns an empty list. |

### Guard against duplicate registration

If your plugin can be reloaded (e.g. via PlugMan), `onEnable()` may run again
while the previous registration is still active. Check before registering:

```java
@Override
public void onEnable() {
    if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
        final boolean alreadyRegistered = TeamsAPI.getSubcommands().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(statsSubcommand.getName()));
        if (!alreadyRegistered) {
            TeamsAPI.registerSubcommand(this, statsSubcommand);
        }
    }
}
```

### Priority and name conflicts

`registerSubcommand` uses `ServicePriority.Normal`. If two plugins register a
subcommand with the same name, the dispatch loop calls the one with the highest
ServicesManager priority first. Prefer unique names to avoid conflicts. Your own
built-in subcommands take priority because the loop runs only after your `switch`
(or equivalent) has already had first pick.

## See also

- [Team Provider](provider-teams): implementing the core `TeamsService`
- [Invite Provider](provider-invites): adding optional invitation support
- [Warp Provider](provider-warps): adding optional warp support
- [API Reference](api): full interface and model documentation
