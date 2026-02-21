package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class GlassCannonLaw implements Law, Listener {
    @Override public String id() { return "GLASS_CANNON"; }
    @Override public String description() { return "+30% dealt, +30% taken"; }

    @Override
    public void onActivate(LawContext ctx) {
        Bukkit.getPluginManager().registerEvents(this, ctx.plugin());
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeal(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) e.setDamage(e.getDamage() * 1.3);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTake(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) e.setDamage(e.getDamage() * 1.3);
    }
}