package com.skyblockexp.teamsapi;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsPowerService;
import com.skyblockexp.teamsapi.event.TeamPowerGainEvent;
import com.skyblockexp.teamsapi.model.PowerGainSource;
import com.skyblockexp.teamsapi.model.Team;

import java.util.Collection;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Schedules a repeating task that grants passive power to online players who are in a team.
 *
 * <p>Each interval, all online players are checked. For each player who belongs to a team,
 * a {@link TeamPowerGainEvent} is fired with source {@link PowerGainSource#PASSIVE}. If the
 * event is not cancelled, {@link TeamsPowerService#addPlayerPower} is called with the
 * (possibly listener-modified) amount.</p>
 *
 * <p>Call {@link #start} from {@link PluginBootstrap#start} and {@link #stop} from
 * {@link PluginBootstrap#stop}.</p>
 *
 * <p><strong>Note:</strong> this class uses {@link org.bukkit.scheduler.BukkitScheduler}
 * and is not compatible with Folia's region scheduler. On Folia, passive regen should be
 * disabled in the configuration.</p>
 */
final class PassivePowerHandler {

    /** Amount of power granted per interval tick. */
    private final double amountPerInterval;

    /** The running scheduler task; {@code null} when not active. */
    private BukkitTask task;

    /**
     * Creates a new {@link PassivePowerHandler}.
     *
     * @param amountPerInterval the power amount granted per interval; must be positive
     */
    PassivePowerHandler(final double amountPerInterval) {
        this.amountPerInterval = amountPerInterval;
    }

    /**
     * Starts the repeating regen task on the Bukkit scheduler.
     *
     * @param plugin        the owning plugin; must not be {@code null}
     * @param intervalTicks the tick interval between regen pulses; typically seconds * 20
     */
    void start(final TeamsApiPlugin plugin, final long intervalTicks) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, intervalTicks, intervalTicks);
    }

    /**
     * Cancels the repeating regen task if it is running.
     */
    void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Executes one regen pulse: iterates all online players and grants power to those
     * who are in a team and whose gain event is not cancelled.
     */
    private void tick() {
        if (!TeamsAPI.isPowerAvailable() || !TeamsAPI.isAvailable()) {
            return;
        }
        final TeamsPowerService power = TeamsAPI.getPowerService();
        final Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        for (final Player player : online) {
            final Optional<Team> teamOpt = TeamsAPI.getService()
                .getPlayerTeam(player.getUniqueId());
            if (teamOpt.isEmpty()) {
                continue;
            }
            final TeamPowerGainEvent event = new TeamPowerGainEvent(
                teamOpt.get(), player.getUniqueId(), amountPerInterval, PowerGainSource.PASSIVE);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                power.addPlayerPower(player.getUniqueId(), event.getAmount());
            }
        }
    }

}
