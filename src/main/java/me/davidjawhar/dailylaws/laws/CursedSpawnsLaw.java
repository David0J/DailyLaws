package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class CursedSpawnsLaw implements Law {
    private final Random r = new Random();
    private BukkitTask task;

    @Override public String id() { return "CURSED_SPAWNS"; }
    @Override public String description() { return "Sometimes spawns a hostile near you"; }

    @Override
    public void onActivate(LawContext ctx) {
        int interval = ctx.plugin().getConfig().getInt("cursed_spawn_interval_seconds", 60);
        int pct = ctx.plugin().getConfig().getInt("cursed_spawn_percent", 15);

        task = Bukkit.getScheduler().runTaskTimer(ctx.plugin(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!affects(p, ctx)) continue;
                if (r.nextInt(100) >= pct) continue;
                Location loc = safeNearby(p.getLocation());
                if (loc == null) continue;
                EntityType type = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER).get(r.nextInt(3));
                p.getWorld().spawnEntity(loc, type);
            }
        }, 40L, interval * 20L);
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        if (task != null) task.cancel();
    }

    private Location safeNearby(Location base) {
        for (int i = 0; i < 12; i++) {
            double dx = (r.nextInt(16) + 8) * (r.nextBoolean() ? 1 : -1);
            double dz = (r.nextInt(16) + 8) * (r.nextBoolean() ? 1 : -1);
            Location l = base.clone().add(dx, 0, dz);
            l.setY(base.getWorld().getHighestBlockYAt(l) + 1);
            if (l.getBlock().isEmpty() && l.clone().add(0,1,0).getBlock().isEmpty()) return l;
        }
        return null;
    }
}