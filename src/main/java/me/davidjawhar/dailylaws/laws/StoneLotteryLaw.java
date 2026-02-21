package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class StoneLotteryLaw implements Law, Listener {
    private final Random r = new Random();
    @Override public String id() { return "STONE_LOTTERY"; }
    @Override public String description() { return "Stone sometimes drops bonus ore"; }

    @Override
    public void onActivate(LawContext ctx) {
        Bukkit.getPluginManager().registerEvents(this, ctx.plugin());
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Material m = e.getBlock().getType();
        if (m != Material.STONE && m != Material.DEEPSLATE) return;

        int pct = e.getPlayer().getServer().getPluginManager().getPlugin("DailyLaws")
                .getConfig().getInt("stone_lottery_percent", 8);
        if (r.nextInt(100) >= pct) return;

        Material drop = rollOre();
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(drop, 1));
    }

    private Material rollOre() {
        int x = r.nextInt(1000);
        if (x < 2) return Material.DIAMOND;           // 0.2%
        if (x < 40) return Material.LAPIS_LAZULI;     // 3.8%
        if (x < 80) return Material.REDSTONE;         // 4.0%
        if (x < 140) return Material.GOLD_INGOT;      // 6.0%
        if (x < 520) return Material.IRON_INGOT;      // 38.0%
        return Material.COAL;                          // 48.0%
    }
}