package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class RandomTpLaw implements Law {
    private final Random r = new Random();
    private BukkitTask task;

    @Override public String id() { return "RANDOM_TP"; }
    @Override public String description() { return "Random small teleport every 5 minutes"; }

    @Override
    public void onActivate(LawContext ctx) {
        int sec = ctx.plugin().getConfig().getInt("events_300s_interval_seconds", 300);
        task = Bukkit.getScheduler().runTaskTimer(ctx.plugin(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!affects(p, ctx)) continue;
                Location safe = safeTeleport(p.getLocation());
                if (safe != null) p.teleport(safe);
            }
        }, 60L, sec * 20L);
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        if (task != null) task.cancel();
    }

    private Location safeTeleport(Location base) {
        for (int i = 0; i < 14; i++) {
            double dx = r.nextInt(31) - 15;
            double dz = r.nextInt(31) - 15;
            Location l = base.clone().add(dx, 0, dz);
            l.setY(base.getWorld().getHighestBlockYAt(l) + 1);
            if (l.getBlock().isEmpty() && l.clone().add(0,1,0).getBlock().isEmpty()) return l;
        }
        return null;
    }
}