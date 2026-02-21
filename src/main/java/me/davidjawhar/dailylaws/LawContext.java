package me.davidjawhar.dailylaws;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class LawContext {
    private final DailyLawsPlugin plugin;

    public LawContext(DailyLawsPlugin plugin) {
        this.plugin = plugin;
    }

    public DailyLawsPlugin plugin() { return plugin; }

    public boolean isWorldEnabled(World w) {
        List<String> worlds = plugin.getConfig().getStringList("enabled_worlds");
        return worlds.contains(w.getName());
    }

    public boolean isPlayerAffected(Player p) {
        if (!isWorldEnabled(p.getWorld())) return false;
        boolean affectCreative = plugin.getConfig().getBoolean("affect_creative", false);
        if (!affectCreative && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR)) return false;
        return true;
    }
}