package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class HotbarShuffleLaw implements Law {
    private BukkitTask task;

    @Override public String id() { return "HOTBAR_SHUFFLE"; }
    @Override public String description() { return "Hotbar shuffles every 5 minutes"; }

    @Override
    public void onActivate(LawContext ctx) {
        int sec = ctx.plugin().getConfig().getInt("events_300s_interval_seconds", 300);
        task = Bukkit.getScheduler().runTaskTimer(ctx.plugin(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!affects(p, ctx)) continue;
                PlayerInventory inv = p.getInventory();
                List<?> slots = new ArrayList<>(Arrays.asList(inv.getContents()).subList(0, 9));
                List<Object> copy = new ArrayList<>();
                for (int i = 0; i < 9; i++) copy.add(inv.getItem(i));
                Collections.shuffle(copy);
                for (int i = 0; i < 9; i++) inv.setItem(i, (org.bukkit.inventory.ItemStack) copy.get(i));
            }
        }, 60L, sec * 20L);
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        if (task != null) task.cancel();
    }
}