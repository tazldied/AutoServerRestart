package cc.tazl.serverrestart;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class restart extends JavaPlugin {

    private static final long RESTART_INTERVAL_TICKS = 5L * 60L * 60L * 20L; // 5 hours
    private static final long FIVE_MINUTES_TICKS = 5L * 60L * 20L;
    private static final long ONE_MINUTE_TICKS = 60L * 20L;
    private static final long TEN_SECONDS_TICKS = 10L * 20L;

    private long nextRestartAtMillis;

    @Override
    public void onEnable() {
        if (getCommand("restarttime") == null) {
            getLogger().severe("Command 'restarttime' is missing from plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("restarttime").setExecutor(this);

        startRestartCycle();
        getLogger().info("ServerRestartPlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ServerRestartPlugin disabled.");
    }

    private void startRestartCycle() {
        final long restartIntervalMillis = RESTART_INTERVAL_TICKS * 50L;
        nextRestartAtMillis = System.currentTimeMillis() + restartIntervalMillis;

        new BukkitRunnable() {
            @Override
            public void run() {
                startCountdown();
                nextRestartAtMillis = System.currentTimeMillis() + restartIntervalMillis;
            }
        }.runTaskTimer(this, RESTART_INTERVAL_TICKS - FIVE_MINUTES_TICKS, RESTART_INTERVAL_TICKS);
    }

    private void startCountdown() {
        broadcastRed("Server will restart in 5 minutes!");

        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastRed("Server will restart in 1 minute!");
            }
        }.runTaskLater(this, FIVE_MINUTES_TICKS - ONE_MINUTE_TICKS);

        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastRed("Server will restart in 10 seconds!");
            }
        }.runTaskLater(this, FIVE_MINUTES_TICKS - TEN_SECONDS_TICKS);

        for (int i = 10; i >= 1; i--) {
            final int seconds = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    broadcastRed("Restarting in " + seconds + "...");
                }
            }.runTaskLater(this, FIVE_MINUTES_TICKS - (seconds * 20L));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastRed("Server is restarting now!");
                Bukkit.shutdown();
            }
        }.runTaskLater(this, FIVE_MINUTES_TICKS);
    }

    private void broadcastRed(String message) {
        Bukkit.broadcast(Component.text(message, NamedTextColor.RED));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("restarttime")) {
            return false;
        }

        long millisLeft = nextRestartAtMillis - System.currentTimeMillis();
        if (millisLeft < 0) {
            millisLeft = 0;
        }

        long totalSeconds = millisLeft / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        String timeMessage = String.format(
                "Time until next restart: %d hours, %d minutes, %d seconds",
                hours, minutes, seconds
        );

        sender.sendMessage(Component.text(timeMessage, NamedTextColor.GREEN));
        return true;
    }
}