package cc.tazl.serverrestart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class restart extends JavaPlugin {

    private static final long RESTART_INTERVAL_TICKS = 5 * 60 * 60 * 20L; // 5 hours
    private static final long FIVE_MINUTES_TICKS = 5 * 60 * 20L;
    private static final long ONE_MINUTE_TICKS = 60 * 20L;
    private static final long TEN_SECONDS_TICKS = 10 * 20L;

    private long nextRestartAtMillis;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getCommand("restarttime") != null) {
            getCommand("restarttime").setExecutor(this);
        } else {
            getLogger().warning("Command 'restarttime' not found in plugin.yml");
        }

        scheduleRestartCycle();
        getLogger().info("Auto server restart enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Auto server restart disabled.");
    }

    private void scheduleRestartCycle() {
        long restartIntervalMillis = RESTART_INTERVAL_TICKS * 50L;
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
        Bukkit.broadcastMessage(ChatColor.RED + "Server will restart in 5 minutes!");

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.RED + "Server will restart in 1 minute!");
            }
        }.runTaskLater(this, FIVE_MINUTES_TICKS - ONE_MINUTE_TICKS);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.RED + "Server will restart in 10 seconds!");
            }
        }.runTaskLater(this, FIVE_MINUTES_TICKS - TEN_SECONDS_TICKS);

        for (int i = 10; i >= 1; i--) {
            final int seconds = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.broadcastMessage(ChatColor.RED + "Restarting in " + seconds + "...");
                }
            }.runTaskLater(this, FIVE_MINUTES_TICKS - (seconds * 20L));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.RED + "Server is restarting now!");
                Bukkit.shutdown();
            }
        }.runTaskLater(this, FIVE_MINUTES_TICKS);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("restarttime")) {
            return false;
        }

        long millisLeft = nextRestartAtMillis - System.currentTimeMillis();
        if (millisLeft < 0) millisLeft = 0;

        long totalSeconds = millisLeft / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        sender.sendMessage(ChatColor.GREEN + String.format(
                "Time until next restart: %d hours, %d minutes, %d seconds",
                hours, minutes, seconds
        ));

        return true;
    }
}