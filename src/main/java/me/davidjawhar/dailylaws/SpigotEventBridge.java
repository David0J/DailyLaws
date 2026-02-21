package me.davidjawhar.dailylaws;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpigotEventBridge implements Listener {
    private final DailyLawsPlugin plugin;
    private final LawManager mgr;

    public SpigotEventBridge(DailyLawsPlugin plugin, LawManager mgr) {
        this.plugin = plugin;
        this.mgr = mgr;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("show_on_join", true)) return;
        e.getPlayer().sendMessage("§6📜 Active Laws: §e" + mgr.getActiveLawSummary());
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if (mgr.isActive("NO_REGEN")) {
            if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                e.setCancelled(true);
            }
        }
    }
}