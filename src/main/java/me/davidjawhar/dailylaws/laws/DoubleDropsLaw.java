package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.*;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class DoubleDropsLaw implements Law, Listener {
    private final Random r = new Random();

    @Override public String id() { return "DOUBLE_DROPS"; }
    @Override public String description() { return "Chance to double block drops"; }

    @Override
    public void onActivate(LawContext ctx) {
        Bukkit.getPluginManager().registerEvents(this, ctx.plugin());
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e) {
        int pct = e.getPlayer().getServer().getPluginManager().getPlugin("DailyLaws")
                .getConfig().getInt("double_drops_percent", 20);
        if (r.nextInt(100) >= pct) return;
        e.getItems().forEach(it -> it.getItemStack().setAmount(it.getItemStack().getAmount() * 2));
    }
}