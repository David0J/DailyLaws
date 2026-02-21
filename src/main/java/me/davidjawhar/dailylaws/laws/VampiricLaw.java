package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.*;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;

public class VampiricLaw implements Law, Listener {
    @Override public String id() { return "VAMPIRIC"; }
    @Override public String description() { return "Killing hostile heals 2 hearts"; }

    @Override
    public void onActivate(LawContext ctx) {
        Bukkit.getPluginManager().registerEvents(this, ctx.plugin());
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Monster)) return;
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;

        double max = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        killer.setHealth(Math.min(max, killer.getHealth() + 4.0));
    }
}