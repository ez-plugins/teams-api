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
 * <p>On Folia, the task is scheduled via {@code GlobalRegionScheduler} rather than
 * {@code BukkitScheduler}. Both paths are selected at runtime via {@link #FOLIA}.</p>
 */
final class PassivePowerHandler {

    /**
     * {@code true} when the server is running Folia (threaded-region scheduler).
     * Detected once at class-load time via a marker class that only Folia ships.
     */
    private static final boolean FOLIA = detectFolia();

    /** Amount of power granted per interval tick. */
    private final double amountPerInterval;

    /**
     * Cancels the running task; {@code null} when no task is active.
     * Stored as a {@link Runnable} so both {@link BukkitTask#cancel()} and
     * {@code ScheduledTask#cancel()} can be held without a shared type.
     */
    private Runnable taskCanceller;

    /**
     * Creates a new {@link PassivePowerHandler}.
     *
     * @param amountPerInterval the power amount granted per interval; must be positive
     */
    PassivePowerHandler(final double amountPerInterval) {
        this.amountPerInterval = amountPerInterval;
    }

    /**
     * Returns {@code true} when the server runtime is Folia.
     *
     * @return {@code true} if Folia is detected
     */
    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Starts the repeating regen task using the appropriate scheduler.
     *
     * <p>On Folia the task is registered with {@code GlobalRegionScheduler};
     * on Paper / Spigot the legacy {@link org.bukkit.scheduler.BukkitScheduler} is used.</p>
     *
     * @param plugin        the owning plugin; must not be {@code null}
     * @param intervalTicks the tick interval between regen pulses; typically seconds * 20
     */
    void start(final TeamsApiPlugin plugin, final long intervalTicks) {
        if (FOLIA) {
            final io.papermc.paper.threadedregions.scheduler.ScheduledTask t =
                plugin.getServer().getGlobalRegionScheduler()
                    .runAtFixedRate(plugin, ignored -> tick(), intervalTicks, intervalTicks);
            taskCanceller = t::cancel;
        }
        else {
            final BukkitTask t = Bukkit.getScheduler()
                .runTaskTimer(plugin, this::tick, intervalTicks, intervalTicks);
            taskCanceller = t::cancel;
        }
    }

    /**
     * Cancels the repeating regen task if it is running.
     */
    void stop() {
        if (taskCanceller != null) {
            taskCanceller.run();
            taskCanceller = null;
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
