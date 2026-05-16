package com.skyblockexp.teamsapi;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.api.TeamsSubcommand;
import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * Handles all startup and shutdown logic for TeamsAPI, including event listener
 * registration, plugin-messaging channel wiring, command processing, and the
 * Velocity bridge dispatcher.
 *
 * <p>Call {@link #start} from {@link TeamsApiPlugin#onEnable} and
 * {@link #stop} from {@link TeamsApiPlugin#onDisable}.</p>
 */
final class PluginBootstrap implements Listener, PluginMessageListener {

    /** Handler for passive power regen; {@code null} when disabled in config. */
    private PassivePowerHandler passiveHandler;

    /** Handler for the power shop; {@code null} when disabled or Vault is absent. */
    private PowerShopHandler shopHandler;

    /**
     * Creates a new bootstrap instance.
     */
    PluginBootstrap() {
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Starts up the plugin: initialises the registry, registers event listeners
     * and plugin-messaging channels, and logs readiness.
     *
     * @param plugin the owning plugin instance
     */
    void start(final TeamsApiPlugin plugin) {
        PluginRegistry.init(plugin);

        PluginRegistry.getLogger().info("TeamsAPI v" + plugin.getDescription().getVersion()
            + " (API " + TeamsAPI.API_VERSION + ") enabled.");
        PluginRegistry.getLogger().info("Waiting for a TeamsService provider to register...");

        Bukkit.getPluginManager().registerEvents(this, plugin);

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(
            plugin, PluginRegistry.BRIDGE_CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(
            plugin, PluginRegistry.BRIDGE_CHANNEL, this);

        if (TeamsAPI.isAvailable()) {
            PluginRegistry.getLogger().info(
                "TeamsService provider registered: "
                + TeamsAPI.getService().getClass().getName());
        }

        plugin.saveDefaultConfig();
        startOptionalFeatures(plugin);
    }

    /**
     * Reads the plugin configuration and starts optional features (passive regen, power shop).
     *
     * @param plugin the owning plugin instance
     */
    private void startOptionalFeatures(final TeamsApiPlugin plugin) {
        final FileConfiguration cfg = plugin.getConfig();

        if (cfg.getBoolean("passive-regen.enabled", false)) {
            final double amount = cfg.getDouble("passive-regen.amount-per-interval", 1.0);
            final long intervalSecs = cfg.getLong("passive-regen.interval-seconds", 60);
            passiveHandler = new PassivePowerHandler(amount);
            passiveHandler.start(plugin, intervalSecs * 20L);
            PluginRegistry.getLogger().info(
                "Passive power regen enabled (every " + intervalSecs + "s, +" + amount + " per tick).");
        }

        if (cfg.getBoolean("power-shop.enabled", false)) {
            final double price = cfg.getDouble("power-shop.price-per-unit", 100.0);
            final double maxBuy = cfg.getDouble("power-shop.max-per-purchase", 10.0);
            try {
                shopHandler = new PowerShopHandler(price, maxBuy);
                if (!shopHandler.init()) {
                    PluginRegistry.getLogger().warning(
                        "Power shop enabled but no Vault economy provider was found.");
                    shopHandler = null;
                }
                else {
                    PluginRegistry.getLogger().info(
                        "Power shop enabled (" + price + " per unit, max " + maxBuy + " per purchase).");
                }
            }
            catch (NoClassDefFoundError ignored) {
                PluginRegistry.getLogger().warning(
                    "Power shop enabled but Vault is not installed. Install Vault to use this feature.");
                shopHandler = null;
            }
        }
    }

    /**
     * Shuts down the plugin: logs provider status and clears the registry.
     */
    void stop() {
        if (passiveHandler != null) {
            passiveHandler.stop();
        }
        if (!TeamsAPI.isAvailable()) {
            PluginRegistry.getLogger().warning(
                "TeamsAPI is shutting down with no provider registered. "
                    + "Plugins depending on TeamsAPI may have degraded or no team features."
            );
        }
        else {
            PluginRegistry.getLogger().info("TeamsAPI disabled. Provider was: "
                + TeamsAPI.getService().getClass().getName());
        }
        PluginRegistry.clear();
    }

    // -------------------------------------------------------------------------
    // Service lifecycle events
    // -------------------------------------------------------------------------

    /**
     * Listens for a {@link TeamsService} being registered via Bukkit's ServicesManager
     * and logs a confirmation message.
     *
     * @param event the service register event
     */
    @EventHandler
    public void onServiceRegister(final ServiceRegisterEvent event) {
        if (!TeamsService.class.equals(event.getProvider().getService())) {
            return;
        }
        PluginRegistry.getLogger().info(
            "TeamsService provider registered: "
            + event.getProvider().getProvider().getClass().getName());
    }

    /**
     * Listens for a {@link TeamsService} being unregistered and logs a warning.
     *
     * @param event the service unregister event
     */
    @EventHandler
    public void onServiceUnregister(final ServiceUnregisterEvent event) {
        if (!TeamsService.class.equals(event.getProvider().getService())) {
            return;
        }

        PluginRegistry.getLogger().warning("TeamsService provider unregistered: "
            + event.getProvider().getProvider().getClass().getName());

        final RegisteredServiceProvider<TeamsService> next =
            Bukkit.getServicesManager().getRegistration(TeamsService.class);

        if (next != null) {
            PluginRegistry.getLogger().info(
                "Fallback provider active: " + next.getProvider().getClass().getName());
        }
        else {
            PluginRegistry.getLogger().warning("No TeamsService provider is now active.");
        }
    }

    // -------------------------------------------------------------------------
    // Command handling
    // -------------------------------------------------------------------------

    /**
     * Processes a {@code /teamsapi} command and returns whether it was handled.
     *
     * @param sender  the command sender
     * @param command the dispatched command
     * @param label   the alias used
     * @param args    the command arguments
     * @return {@code true} when the command was recognised and handled
     */
    boolean handleCommand(final CommandSender sender, final Command command,
            final String label, final String[] args) {
        if (!command.getName().equalsIgnoreCase("teamsapi")) {
            return false;
        }

        if (!sender.hasPermission("teamsapi.use")) {
            sender.sendMessage("[TeamsAPI] You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage("[TeamsAPI] Plugin: v"
                + PluginRegistry.getPlugin().getDescription().getVersion()
                + " | API: v" + TeamsAPI.API_VERSION);
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            if (!sender.hasPermission("teamsapi.status")) {
                sender.sendMessage("[TeamsAPI] You do not have permission to use this command.");
                return true;
            }
            sendStatus(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (!sender.hasPermission("teamsapi.admin")) {
                sender.sendMessage("[TeamsAPI] You do not have permission to use this command.");
                return true;
            }
            sendInfo(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("power")) {
            handlePowerCommand(sender, args);
            return true;
        }

        for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
            if (sub.getName().equalsIgnoreCase(args[0])) {
                final String perm = sub.getPermission();
                if (perm != null && !sender.hasPermission(perm)) {
                    sender.sendMessage("[TeamsAPI] You do not have permission to use this command.");
                    return true;
                }
                sub.execute(sender, args);
                return true;
            }
        }

        sendHelp(sender);
        return true;
    }

    /**
     * Handles tab-completion for the {@code /teamsapi} command.
     *
     * <p>Returns the matching built-in subcommand names when completing the
     * first argument. Delegates to {@link TeamsSubcommand#tabComplete} for
     * registered custom subcommands when completing further arguments.</p>
     *
     * @param sender  the command sender requesting completions
     * @param command the dispatched command
     * @param label   the alias used
     * @param args    the arguments typed so far
     * @return a list of completion strings; never {@code null}
     */
    List<String> handleTabComplete(final CommandSender sender, final Command command,
            final String label, final String[] args) {
        if (!command.getName().equalsIgnoreCase("teamsapi")) {
            return Collections.emptyList();
        }
        if (!sender.hasPermission("teamsapi.use")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filterByPrefix(buildTopLevelCompletions(sender), args[0]);
        }
        if (args[0].equalsIgnoreCase("power") && args.length == 2) {
            return filterByPrefix(buildPowerCompletions(sender), args[1]);
        }
        for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
            if (sub.getName().equalsIgnoreCase(args[0])) {
                final String perm = sub.getPermission();
                if (perm != null && !sender.hasPermission(perm)) {
                    return Collections.emptyList();
                }
                return sub.tabComplete(sender, args);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Builds the list of top-level subcommand names visible to the given sender.
     *
     * @param sender the command sender
     * @return a mutable list of completion candidates
     */
    private static List<String> buildTopLevelCompletions(final CommandSender sender) {
        final List<String> completions = new ArrayList<>();
        completions.add("version");
        completions.add("help");
        if (sender.hasPermission("teamsapi.status")) {
            completions.add("status");
        }
        if (sender.hasPermission("teamsapi.admin")) {
            completions.add("info");
        }
        if (sender.hasPermission("teamsapi.power")) {
            completions.add("power");
        }
        for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
            final String perm = sub.getPermission();
            if (perm == null || sender.hasPermission(perm)) {
                completions.add(sub.getName());
            }
        }
        return completions;
    }

    /**
     * Builds the list of {@code /teamsapi power} sub-argument completions visible
     * to the given sender.
     *
     * @param sender the command sender
     * @return a mutable list of completion candidates
     */
    private static List<String> buildPowerCompletions(final CommandSender sender) {
        final List<String> completions = new ArrayList<>();
        completions.add("status");
        if (sender.hasPermission("teamsapi.power.buy")) {
            completions.add("buy");
        }
        return completions;
    }

    /**
     * Filters a list of completion candidates by a case-insensitive prefix.
     *
     * @param candidates the full candidate list
     * @param prefix     the partial input to filter by
     * @return a new list containing only candidates that start with the prefix
     */
    private static List<String> filterByPrefix(final List<String> candidates,
            final String prefix) {
        final String lower = prefix.toLowerCase();
        final List<String> result = new ArrayList<>();
        for (final String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(lower)) {
                result.add(candidate);
            }
        }
        return result;
    }

    /**
     * Sends the help message to the given sender.
     *
     * @param sender the command sender to message
     */
    private static void sendHelp(final CommandSender sender) {
        sender.sendMessage("[TeamsAPI] Commands:");
        sender.sendMessage("  /teamsapi version          - Show version info");
        if (sender.hasPermission("teamsapi.status")) {
            sender.sendMessage("  /teamsapi status           - Show TeamsAPI status");
        }
        if (sender.hasPermission("teamsapi.admin")) {
            sender.sendMessage("  /teamsapi info             - Show detailed provider info (op)");
        }
        if (sender.hasPermission("teamsapi.power")) {
            sender.sendMessage("  /teamsapi power status     - Show your current power");
        }
        if (sender.hasPermission("teamsapi.power.buy")) {
            sender.sendMessage("  /teamsapi power buy <n>    - Purchase power (requires Vault)");
        }
        for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
            final String perm = sub.getPermission();
            if (perm == null || sender.hasPermission(perm)) {
                sender.sendMessage("  /teamsapi " + sub.getName() + " - " + sub.getDescription());
            }
        }
    }

    /**
     * Handles the {@code /teamsapi power} subcommand tree.
     *
     * @param sender the command sender
     * @param args   the full argument array from the parent command (args[0] is "power")
     */
    private void handlePowerCommand(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("teamsapi.power")) {
            sender.sendMessage("[TeamsAPI] You do not have permission to use this command.");
            return;
        }
        if (args.length < 2 || args[1].equalsIgnoreCase("status")) {
            handlePowerStatus(sender);
            return;
        }
        if (args[1].equalsIgnoreCase("buy")) {
            handlePowerBuy(sender, args);
            return;
        }
        sender.sendMessage("[TeamsAPI] Usage: /teamsapi power [status|buy <amount>]");
    }

    /**
     * Handles {@code /teamsapi power status}: shows the sender's current and max power.
     *
     * @param sender the command sender; must be a {@link Player}
     */
    private static void handlePowerStatus(final CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[TeamsAPI] This command can only be used by players.");
            return;
        }
        if (!TeamsAPI.isPowerAvailable()) {
            sender.sendMessage("[TeamsAPI] The power system is currently unavailable.");
            return;
        }
        final Player player = (Player) sender;
        final double current = TeamsAPI.getPowerService().getPlayerPower(player.getUniqueId());
        final double max = TeamsAPI.getPowerService().getPlayerMaxPower(player.getUniqueId());
        sender.sendMessage("[TeamsAPI] Your power: " + current + " / " + max);
    }

    /**
     * Handles {@code /teamsapi power buy <amount>}: purchases power via Vault economy.
     *
     * @param sender the command sender; must be a {@link Player}
     * @param args   the full argument array (args[2] is the amount string)
     */
    private void handlePowerBuy(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[TeamsAPI] This command can only be used by players.");
            return;
        }
        if (!sender.hasPermission("teamsapi.power.buy")) {
            sender.sendMessage("[TeamsAPI] You do not have permission to buy power.");
            return;
        }
        if (shopHandler == null) {
            sender.sendMessage("[TeamsAPI] The power shop is not enabled on this server.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("[TeamsAPI] Usage: /teamsapi power buy <amount>");
            return;
        }
        final double amount;
        try {
            amount = Double.parseDouble(args[2]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage("[TeamsAPI] Invalid amount: " + args[2]);
            return;
        }
        final String error = shopHandler.buy((Player) sender, amount);
        if (error != null) {
            sender.sendMessage("[TeamsAPI] " + error);
            return;
        }
        sender.sendMessage("[TeamsAPI] Successfully purchased " + amount + " power.");
    }

    /**
     * Sends a brief, player-friendly status summary to the given sender.
     *
     * <p>Shows the active provider, team count, and which optional services
     * are currently registered. No admin permission is required.</p>
     *
     * @param sender the command sender to message
     */
    private static void sendStatus(final CommandSender sender) {
        if (!TeamsAPI.isAvailable()) {
            sender.sendMessage("[TeamsAPI] No team plugin is currently active.");
            return;
        }
        final TeamsService service = TeamsAPI.getService();
        sender.sendMessage("[TeamsAPI] Status: active");
        sender.sendMessage("[TeamsAPI] Provider: " + service.getClass().getSimpleName());
        sender.sendMessage("[TeamsAPI] Teams: " + service.getTeamCount());
        final StringBuilder services = new StringBuilder("teams");
        if (TeamsAPI.isInviteAvailable()) {
            services.append(", invites");
        }
        if (TeamsAPI.isWarpAvailable()) {
            services.append(", warps");
        }
        if (TeamsAPI.isClaimAvailable()) {
            services.append(", claims");
        }
        if (TeamsAPI.isPowerAvailable()) {
            services.append(", power");
        }
        sender.sendMessage("[TeamsAPI] Services: " + services);
    }

    /**
     * Sends provider information to the given sender.
     *
     * @param sender the command sender to message
     */
    private static void sendInfo(final CommandSender sender) {
        sender.sendMessage("[TeamsAPI] API Version: " + TeamsAPI.API_VERSION);
        if (!TeamsAPI.isAvailable()) {
            sender.sendMessage("[TeamsAPI] TeamsService:  none");
        }
        else {
            final TeamsService service = TeamsAPI.getService();
            sender.sendMessage("[TeamsAPI] TeamsService:  " + service.getClass().getName());
            sender.sendMessage("[TeamsAPI] Teams loaded: " + service.getTeamCount());
        }
        final String inviteInfo = TeamsAPI.isInviteAvailable()
            ? TeamsAPI.getInviteService().getClass().getName() : "none";
        sender.sendMessage("[TeamsAPI] InviteService: " + inviteInfo);
        final String warpInfo = TeamsAPI.isWarpAvailable()
            ? TeamsAPI.getWarpService().getClass().getName() : "none";
        sender.sendMessage("[TeamsAPI] WarpService:   " + warpInfo);
        final String claimInfo = TeamsAPI.isClaimAvailable()
            ? TeamsAPI.getClaimService().getClass().getName() : "none";
        sender.sendMessage("[TeamsAPI] ClaimService:  " + claimInfo);
        final String powerInfo = TeamsAPI.isPowerAvailable()
            ? TeamsAPI.getPowerService().getClass().getName() : "none";
        sender.sendMessage("[TeamsAPI] PowerService:  " + powerInfo);
        final Collection<TeamsSubcommand> subs = TeamsAPI.getSubcommands();
        if (!subs.isEmpty()) {
            sender.sendMessage("[TeamsAPI] Subcommands: " + subs.size() + " registered");
        }
    }

    // -------------------------------------------------------------------------
    // Velocity bridge — plugin messaging handler
    // -------------------------------------------------------------------------

    /**
     * Handles an incoming plugin message on the {@code teamsapi:bridge} channel.
     *
     * <p>Parses the JSON request from the Velocity proxy, dispatches it to the
     * registered {@link TeamsService}, and sends a JSON response back through
     * the same player's connection.</p>
     *
     * @param channel the channel the message arrived on
     * @param player  the player whose connection carried the message
     * @param message the raw UTF-8 JSON request bytes
     */
    @Override
    public void onPluginMessageReceived(final String channel, final Player player,
            final byte[] message) {
        if (!PluginRegistry.BRIDGE_CHANNEL.equals(channel)) {
            return;
        }
        final String json = new String(message, StandardCharsets.UTF_8);
        final String reqId = jsonStr(json, "reqId");
        if (reqId == null) {
            return;
        }
        final String op = jsonStr(json, "op");
        if (op == null) {
            return;
        }
        final String response = dispatchBridgeOp(reqId, op, json);
        player.sendPluginMessage(
            PluginRegistry.getPlugin(), PluginRegistry.BRIDGE_CHANNEL,
            response.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Dispatches a bridge operation and returns the JSON response string.
     *
     * @param reqId the request identifier to echo in the response
     * @param op    the operation name
     * @param json  the full request JSON (for argument extraction)
     * @return the JSON response string
     */
    private static String dispatchBridgeOp(final String reqId, final String op,
            final String json) {
        if (!TeamsAPI.isAvailable()) {
            return errResp(reqId, "No TeamsService registered");
        }
        final TeamsService svc = TeamsAPI.getService();
        if ("hasTeam".equals(op)) {
            return handleHasTeam(reqId, json, svc);
        }
        else if ("getPlayerTeam".equals(op)) {
            return handleGetPlayerTeam(reqId, json, svc);
        }
        else if ("getTeam".equals(op)) {
            return handleGetTeam(reqId, json, svc);
        }
        else if ("getTeamByName".equals(op)) {
            return handleGetTeamByName(reqId, json, svc);
        }
        else if ("getTeamCount".equals(op)) {
            return okResp(reqId, "\"count\":" + svc.getTeamCount());
        }
        else if ("getAllTeams".equals(op)) {
            return handleGetAllTeams(reqId, svc);
        }
        else if ("teamExists".equals(op)) {
            return handleTeamExists(reqId, json, svc);
        }
        else if ("isMember".equals(op)) {
            return handleIsMember(reqId, json, svc);
        }
        else if ("getMemberRole".equals(op)) {
            return handleGetMemberRole(reqId, json, svc);
        }
        else if ("getMemberInfo".equals(op)) {
            return handleGetMemberInfo(reqId, json, svc);
        }
        return errResp(reqId, "Unknown operation: " + op);
    }

    /**
     * Handles the {@code hasTeam} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleHasTeam(final String reqId, final String json,
            final TeamsService svc) {
        final String uuidStr = jsonStr(json, "playerUuid");
        if (uuidStr == null) {
            return errResp(reqId, "Missing playerUuid");
        }
        final boolean result = svc.hasTeam(UUID.fromString(uuidStr));
        return okResp(reqId, "\"result\":" + result);
    }

    /**
     * Handles the {@code getPlayerTeam} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleGetPlayerTeam(final String reqId, final String json,
            final TeamsService svc) {
        final String uuidStr = jsonStr(json, "playerUuid");
        if (uuidStr == null) {
            return errResp(reqId, "Missing playerUuid");
        }
        final Optional<Team> team = svc.getPlayerTeam(UUID.fromString(uuidStr));
        return teamOptResp(reqId, team);
    }

    /**
     * Handles the {@code getTeam} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleGetTeam(final String reqId, final String json,
            final TeamsService svc) {
        final String idStr = jsonStr(json, "teamId");
        if (idStr == null) {
            return errResp(reqId, "Missing teamId");
        }
        final UUID teamId;
        try {
            teamId = UUID.fromString(idStr);
        }
        catch (IllegalArgumentException e) {
            return errResp(reqId, "Invalid teamId");
        }
        return teamOptResp(reqId, svc.getTeam(teamId));
    }

    /**
     * Handles the {@code getTeamByName} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleGetTeamByName(final String reqId, final String json,
            final TeamsService svc) {
        final String name = jsonStr(json, "name");
        if (name == null) {
            return errResp(reqId, "Missing name");
        }
        return teamOptResp(reqId, svc.getTeamByName(name));
    }

    /**
     * Handles the {@code getAllTeams} operation.
     *
     * @param reqId the request ID
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleGetAllTeams(final String reqId, final TeamsService svc) {
        final Collection<Team> teams = svc.getAllTeams();
        final StringBuilder sb = new StringBuilder("\"teams\":[");
        boolean first = true;
        for (final Team t : teams) {
            if (!first) {
                sb.append(",");
            }
            sb.append("{").append(teamFields(t)).append("}");
            first = false;
        }
        sb.append("]");
        return okResp(reqId, sb.toString());
    }

    /**
     * Handles the {@code teamExists} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleTeamExists(final String reqId, final String json,
            final TeamsService svc) {
        final String name = jsonStr(json, "name");
        if (name == null) {
            return errResp(reqId, "Missing name");
        }
        return okResp(reqId, "\"result\":" + svc.teamExists(name));
    }

    /**
     * Handles the {@code isMember} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleIsMember(final String reqId, final String json,
            final TeamsService svc) {
        final String teamIdStr = jsonStr(json, "teamId");
        final String playerUuidStr = jsonStr(json, "playerUuid");
        if (teamIdStr == null || playerUuidStr == null) {
            return errResp(reqId, "Missing teamId or playerUuid");
        }
        final boolean result = svc.isMember(UUID.fromString(teamIdStr),
            UUID.fromString(playerUuidStr));
        return okResp(reqId, "\"result\":" + result);
    }

    /**
     * Handles the {@code getMemberRole} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleGetMemberRole(final String reqId, final String json,
            final TeamsService svc) {
        final String teamIdStr = jsonStr(json, "teamId");
        final String playerUuidStr = jsonStr(json, "playerUuid");
        if (teamIdStr == null || playerUuidStr == null) {
            return errResp(reqId, "Missing teamId or playerUuid");
        }
        final Optional<TeamRole> role = svc.getMemberRole(UUID.fromString(teamIdStr),
            UUID.fromString(playerUuidStr));
        if (role.isEmpty()) {
            return okResp(reqId, "\"present\":false");
        }
        return okResp(reqId, "\"present\":true,\"role\":\"" + role.get().name() + "\"");
    }

    /**
     * Handles the {@code getMemberInfo} operation.
     *
     * @param reqId the request ID
     * @param json  the request JSON
     * @param svc   the active service
     * @return the JSON response
     */
    private static String handleGetMemberInfo(final String reqId, final String json,
            final TeamsService svc) {
        final String teamIdStr = jsonStr(json, "teamId");
        final String playerUuidStr = jsonStr(json, "playerUuid");
        if (teamIdStr == null || playerUuidStr == null) {
            return errResp(reqId, "Missing teamId or playerUuid");
        }
        final Optional<TeamMember> info = svc.getMemberInfo(UUID.fromString(teamIdStr),
            UUID.fromString(playerUuidStr));
        if (info.isEmpty()) {
            return okResp(reqId, "\"present\":false");
        }
        final TeamMember m = info.get();
        return okResp(reqId,
            "\"present\":true"
            + ",\"memberUuid\":\"" + m.getPlayerUUID() + "\""
            + ",\"role\":\"" + m.getRole().name() + "\""
            + ",\"joinedAt\":" + m.getJoinedAt().toEpochMilli());
    }

    // -------------------------------------------------------------------------
    // JSON helpers — no external library; protocol values are UUIDs and names
    // -------------------------------------------------------------------------

    /**
     * Serialises the fields of a {@link Team} into a flat JSON fragment
     * (without outer braces).
     *
     * @param t the team to serialise
     * @return a JSON key-value fragment, e.g. {@code "id":"...","name":"..."}
     */
    private static String teamFields(final Team t) {
        return "\"id\":\"" + t.getId() + "\""
            + ",\"name\":\"" + jsonEsc(t.getName()) + "\""
            + ",\"displayName\":\"" + jsonEsc(t.getDisplayName()) + "\""
            + ",\"ownerUuid\":\"" + t.getOwnerUUID() + "\""
            + ",\"size\":" + t.getSize()
            + ",\"maxSize\":" + t.getMaxSize();
    }

    /**
     * Returns an {@code ok=true} response containing the given team, or {@code present=false}
     * when the optional is empty.
     *
     * @param reqId the request ID
     * @param team  the optional team result
     * @return the JSON response
     */
    private static String teamOptResp(final String reqId, final Optional<Team> team) {
        if (team.isEmpty()) {
            return okResp(reqId, "\"present\":false");
        }
        return okResp(reqId, "\"present\":true," + teamFields(team.get()));
    }

    /**
     * Builds an {@code ok=true} JSON response with additional body fields.
     *
     * @param reqId the request ID
     * @param body  the additional JSON fields to append after {@code "ok":true}
     * @return the complete JSON response string
     */
    private static String okResp(final String reqId, final String body) {
        return "{\"reqId\":\"" + reqId + "\",\"ok\":true," + body + "}";
    }

    /**
     * Builds an {@code ok=false} JSON error response.
     *
     * @param reqId   the request ID
     * @param message the error message
     * @return the complete JSON response string
     */
    private static String errResp(final String reqId, final String message) {
        return "{\"reqId\":\"" + reqId + "\",\"ok\":false,\"error\":\"" + jsonEsc(message) + "\"}";
    }

    /**
     * Extracts the value of a simple quoted JSON string field from a flat JSON object.
     * Only handles non-nested string values. Returns {@code null} when the field is absent.
     *
     * @param json the JSON object string
     * @param key  the field name to look up
     * @return the unescaped string value, or {@code null}
     */
    private static String jsonStr(final String json, final String key) {
        final String search = "\"" + key + "\":\"";
        final int start = json.indexOf(search);
        if (start < 0) {
            return null;
        }
        final int valStart = start + search.length();
        final int end = json.indexOf("\"", valStart);
        if (end < 0) {
            return null;
        }
        return json.substring(valStart, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    /**
     * Escapes backslashes and double-quote characters for embedding in a JSON string value.
     *
     * @param value the raw string
     * @return the JSON-safe escaped string
     */
    private static String jsonEsc(final String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
