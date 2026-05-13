package com.skyblockexp.teamsapi;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsPowerService;
import com.skyblockexp.teamsapi.event.TeamPowerGainEvent;
import com.skyblockexp.teamsapi.model.PowerGainSource;
import com.skyblockexp.teamsapi.model.Team;

import java.util.Optional;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Handles power-shop logic for the {@code /teamsapi power buy} command.
 *
 * <p>Requires Vault to be installed and an economy provider to be registered.
 * Call {@link #init()} after construction; if it returns {@code false} the shop
 * is not available and no buy operations should be attempted.</p>
 *
 * <p>This class is package-private and instantiated only from
 * {@link PluginBootstrap} after a class-loading guard confirms that Vault is
 * present on the classpath.</p>
 */
final class PowerShopHandler {

    /** Cost in economy units per 1.0 unit of power. */
    private final double pricePerUnit;

    /** Maximum power a player may purchase in one invocation. */
    private final double maxPerPurchase;

    /** The active economy provider; {@code null} until {@link #init()} succeeds. */
    private Economy economy;

    /**
     * Creates a new {@link PowerShopHandler}.
     *
     * @param pricePerUnit   the economy cost per 1.0 unit of power; must be positive
     * @param maxPerPurchase the maximum power purchasable in a single command; must be positive
     */
    PowerShopHandler(final double pricePerUnit, final double maxPerPurchase) {
        this.pricePerUnit = pricePerUnit;
        this.maxPerPurchase = maxPerPurchase;
    }

    /**
     * Looks up a registered Vault {@link Economy} provider and stores it for later use.
     *
     * @return {@code true} if an economy provider was found; {@code false} otherwise
     */
    boolean init() {
        final RegisteredServiceProvider<Economy> rsp =
            Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    /**
     * Returns whether the shop is ready to process purchases.
     *
     * @return {@code true} if {@link #init()} succeeded and an economy is available
     */
    boolean isReady() {
        return economy != null;
    }

    /**
     * Attempts to sell {@code amount} units of power to the given player.
     *
     * <p>The method checks, in order:</p>
     * <ol>
     *   <li>The requested amount is positive and within {@link #maxPerPurchase}.</li>
     *   <li>A {@link TeamsPowerService} provider is registered.</li>
     *   <li>The player belongs to a team.</li>
     *   <li>The player has sufficient balance for the computed cost.</li>
     *   <li>A {@link TeamPowerGainEvent} is fired; the purchase is aborted if cancelled.</li>
     * </ol>
     *
     * @param player the buyer; must not be {@code null}
     * @param amount the requested power amount; must be positive
     * @return an error message string if the purchase failed, or {@code null} on success
     */
    String buy(final Player player, final double amount) {
        if (amount <= 0) {
            return "Amount must be positive.";
        }
        if (amount > maxPerPurchase) {
            return "You may purchase at most " + maxPerPurchase + " power per command.";
        }
        if (!TeamsAPI.isPowerAvailable()) {
            return "The power system is currently unavailable.";
        }
        if (!TeamsAPI.isAvailable()) {
            return "The teams system is currently unavailable.";
        }
        final Optional<Team> teamOpt = TeamsAPI.getService()
            .getPlayerTeam(player.getUniqueId());
        if (teamOpt.isEmpty()) {
            return "You must be in a team to purchase power.";
        }
        final double cost = amount * pricePerUnit;
        if (!economy.has(player, cost)) {
            return "Insufficient funds. This purchase costs " + cost + ".";
        }
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            teamOpt.get(), player.getUniqueId(), amount, PowerGainSource.PURCHASE);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return "Power purchase was denied.";
        }
        economy.withdrawPlayer(player, cost);
        final TeamsPowerService power = TeamsAPI.getPowerService();
        power.addPlayerPower(player.getUniqueId(), event.getAmount());
        return null;
    }

}
